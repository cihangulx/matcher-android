package com.flort.evlilik.network.socket

import android.util.Log
import com.google.gson.Gson
import com.flort.evlilik.models.message.*
import com.flort.evlilik.models.message.core.Message
import com.flort.evlilik.models.message.core.ReplyMessage
import com.flort.evlilik.models.message.socket.MessageStatusUpdate
import com.flort.evlilik.models.message.socket.SocketMessageEvent
import com.flort.evlilik.models.message.socket.TypingStatus
import com.flort.evlilik.models.message.socket.UserStatus
import com.flort.evlilik.network.Routes
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException
import java.util.UUID

class SocketManager private constructor() {
    
    companion object {
        // Socket.IO server ayrı port'ta çalışıyor (3001)
        private val SOCKET_URL = Routes.SOCKET_URL
        private const val TAG = "SocketManager"
        
        @Volatile
        private var INSTANCE: SocketManager? = null
        
        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }
    
    private var socket: Socket? = null
    private var currentUserId: String? = null
    private val gson = Gson()
    
    // Connection state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected
    
    // Incoming messages
    private val _incomingMessages = MutableStateFlow<SocketMessageEvent?>(null)
    val incomingMessages: StateFlow<SocketMessageEvent?> = _incomingMessages
    
    // Message status updates
    private val _messageStatusUpdates = MutableStateFlow<MessageStatusUpdate?>(null)
    val messageStatusUpdates: StateFlow<MessageStatusUpdate?> = _messageStatusUpdates
    
    // Typing status
    private val _typingStatusUpdates = MutableStateFlow<TypingStatus?>(null)
    val typingStatusUpdates: StateFlow<TypingStatus?> = _typingStatusUpdates
    
    // Online/Offline status
    private val _onlineStatusUpdates = MutableStateFlow<UserStatus?>(null)
    val onlineStatusUpdates: StateFlow<UserStatus?> = _onlineStatusUpdates
    
    /**
     * Socket bağlantısını başlat
     */
    fun connect(authToken: String, userId: String) {
        if (socket?.connected() == true) {
            Log.d(TAG, "Socket zaten bağlı")
            return
        }
        
        this.currentUserId = userId
        
        try {
            val options = IO.Options().apply {
                auth = mapOf("token" to authToken)
                transports = arrayOf("websocket", "polling")
                reconnection = true
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                reconnectionAttempts = 5
                secure = true
            }
            
            socket = IO.socket(SOCKET_URL, options)
            setupListeners()
            socket?.connect()
            
            Log.d(TAG, "Socket bağlantısı başlatılıyor...")
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Socket URL hatası", e)
        } catch (e: Exception) {
            Log.e(TAG, "Socket bağlantı hatası", e)
        }
    }
    
    /**
     * Socket event listener'larını kur
     */
    private fun setupListeners() {
        socket?.apply {
            // Bağlantı başarılı
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Socket bağlantısı başarılı")
                _isConnected.value = true
            }
            
            // Bağlantı koptu
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Socket bağlantısı koptu")
                _isConnected.value = false
            }
            
            // Bağlantı hatası
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()
                Log.e(TAG, "❌ Bağlantı hatası: $error")
                _isConnected.value = false
            }
            
            // Yeniden bağlanıyor
            on("reconnect") {
                Log.d(TAG, "🔄 Yeniden bağlanıldı")
                _isConnected.value = true
            }
            
            // --- MESAJ EVENT'LERİ ---
            
            // Mesaj geldi
            on("message:receive") { args ->
                try {
                    val data = args[0] as JSONObject
                    val messageJson = data.getJSONObject("message")
                    val conversationId = data.getString("conversationId")
                    
                    Log.d(TAG, "📨 API'den gelen mesaj JSON: $messageJson")
                    Log.d(TAG, "📨 Mesaj alındı, replyTo kontrol ediliyor...")
                    Log.d(TAG, "📨 Message JSON: $messageJson")
                    
                    val replyTo = if (messageJson.has("replyTo") && !messageJson.isNull("replyTo")) {
                        Log.d(TAG, "🔍 ReplyTo var, parsing ediliyor...")
                        val replyJson = messageJson.getJSONObject("replyTo")
                        Log.d(TAG, "🔍 ReplyTo JSON: $replyJson")
                        if (
                            replyJson.has("_id") &&
                            replyJson.has("senderId") &&
                            replyJson.has("content") &&
                            replyJson.has("type")
                        ) {
                            val replyMessage = ReplyMessage(
                                id = replyJson.getString("_id"),
                                senderId = replyJson.getString("senderId"),
                                senderName = replyJson.optString("senderName", null),
                                content = replyJson.getString("content"),
                                type = replyJson.getString("type"),
                                mediaUrl = replyJson.optString("mediaUrl", null)
                                    .takeIf { it != "null" }
                            )
                            Log.d(TAG, "✅ ReplyTo başarıyla parse edildi: ${replyMessage.content}")
                            replyMessage
                        } else {
                            Log.w(TAG, "❌ ReplyTo JSON'da gerekli alanlar eksik")
                            null
                        }
                    } else {
                        Log.d(TAG, "ℹ️ ReplyTo yok veya null")
                        null
                    }
                    
                    val message = Message(
                        id = messageJson.getString("_id"),
                        conversationId = messageJson.getString("conversationId"),
                        senderId = messageJson.getString("senderId"),
                        receiverId = messageJson.getString("receiverId"),
                        senderName = messageJson.optString("senderName", null),
                        receiverName = messageJson.optString("receiverName", null),
                        senderPhoto = messageJson.optString("senderPhoto", null)
                            .takeIf { it != "null" },
                        receiverPhoto = messageJson.optString("receiverPhoto", null)
                            .takeIf { it != "null" },
                        content = messageJson.getString("content"),
                        type = messageJson.getString("type"),
                        mediaUrl = messageJson.optString("mediaUrl", null).takeIf { it != "null" },
                        status = messageJson.getString("status"),
                        failReason = messageJson.optString("failReason", null)
                            .takeIf { it != "null" },
                        deliveredAt = messageJson.optString("deliveredAt", null)
                            .takeIf { it != "null" },
                        readAt = messageJson.optString("readAt", null).takeIf { it != "null" },
                        replyTo = replyTo,
                        giftId = messageJson.optString("giftId", null).takeIf { it != "null" }
                            .also {
                                Log.d(TAG, "🎁 Parse edilen giftId: $it")
                            },
                        createdAt = messageJson.getString("createdAt"),
                        updatedAt = messageJson.optString("updatedAt", null).takeIf { it != "null" }
                    )
                    
                    _incomingMessages.value = SocketMessageEvent(message, conversationId)
                    
                    Log.d(TAG, "📨 Yeni mesaj alındı: ${message.id} - ${message.content}")
                } catch (e: Exception) {
                    Log.e(TAG, "Mesaj parse hatası", e)
                }
            }
            
            // Mesaj durumu güncellendi
            on("message:status") { args ->
                try {
                    val data = args[0] as JSONObject
                    val update = MessageStatusUpdate(
                        messageId = data.getString("messageId"),
                        status = data.getString("status"),
                        tempId = data.optString("tempId", null).takeIf { it != "null" },
                        deliveredAt = data.optString("deliveredAt", null).takeIf { it != "null" },
                        readAt = data.optString("readAt", null).takeIf { it != "null" },
                        failReason = data.optString("failReason", null).takeIf { it != "null" }
                    )
                    
                    _messageStatusUpdates.value = update
                    Log.d(TAG, "📊 Mesaj durumu: ${update.status}")
                } catch (e: Exception) {
                    Log.e(TAG, "Status parse hatası", e)
                }
            }
            
            // Konuşma okundu
            on("conversation:read") { args ->
                try {
                    val data = args[0] as JSONObject
                    val conversationId = data.getString("conversationId")
                    val readBy = data.getString("readBy")
                    Log.d(TAG, "📖 Konuşma okundu: $conversationId by $readBy")
                } catch (e: Exception) {
                    Log.e(TAG, "Conversation read parse hatası", e)
                }
            }
            
            // --- TYPING EVENT'LERİ ---
            
            // Yazıyor durumu
            on("typing:status") { args ->
                try {
                    val data = args[0] as JSONObject
                    val status = TypingStatus(
                        userId = data.getString("userId"),
                        isTyping = data.getBoolean("isTyping")
                    )
                    
                    _typingStatusUpdates.value = status
                    Log.d(TAG, "⌨️ Typing: ${status.userId} - ${status.isTyping}")
                } catch (e: Exception) {
                    Log.e(TAG, "Typing parse hatası", e)
                }
            }
            
            // --- ONLINE/OFFLINE EVENT'LERİ ---
            
            // Kullanıcı online
            on("user:online") { args ->
                try {
                    val data = args[0] as JSONObject
                    _onlineStatusUpdates.value = UserStatus(
                        userId = data.getString("userId"),
                        isOnline = true
                    )
                    Log.d(TAG, "🟢 Kullanıcı online")
                } catch (e: Exception) {
                    Log.e(TAG, "User online parse hatası", e)
                }
            }
            
            // Kullanıcı offline
            on("user:offline") { args ->
                try {
                    val data = args[0] as JSONObject
                    _onlineStatusUpdates.value = UserStatus(
                        userId = data.getString("userId"),
                        isOnline = false
                    )
                    Log.d(TAG, "🔴 Kullanıcı offline")
                } catch (e: Exception) {
                    Log.e(TAG, "User offline parse hatası", e)
                }
            }
        }
    }
    
    /**
     * Mesaj gönder
     */
    fun sendMessage(
        receiverId: String,
        content: String,
        type: String = "text",
        mediaUrl: String? = null,
        tempId: String = UUID.randomUUID().toString(),
        resendMessageId: String? = null, // Tekrar gönderilen mesajın ID'si
        replyTo: ReplyMessage? = null, // Reply edilen mesaj
        giftId: String? = null, // Hediye ID'si
        callback: (SendMessageResponse) -> Unit
    ) {
        if (!isConnected.value) {
            callback(SendMessageResponse.Error("Socket bağlı değil"))
            return
        }
        
        val data = JSONObject().apply {
            put("receiverId", receiverId)
            put("content", content)
            put("type", type)
            put("tempId", tempId)
            if (mediaUrl != null) put("mediaUrl", mediaUrl)
            if (resendMessageId != null) put("resendMessageId", resendMessageId) // Tekrar gönderilen mesaj ID'si
            if (replyTo != null) {
                put("replyTo", replyTo.id) // Sadece tek ID değeri gönder
            }
            if (giftId != null) put("giftId", giftId) // Hediye ID'si
        }
        
        Log.d(TAG, "📤 API'ye gönderilen data: $data")
        
        socket?.emit("message:send", data, Ack { args ->
            try {
                val response = args[0] as JSONObject
                val success = response.getBoolean("success")
                
                if (success) {
                    val messageJson = response.getJSONObject("message")
                    
                    val replyTo = if (messageJson.has("replyTo") && !messageJson.isNull("replyTo")) {
                        val replyJson = messageJson.getJSONObject("replyTo")
                        if (
                            replyJson.has("_id") &&
                            replyJson.has("senderId") &&
                            replyJson.has("content") &&
                            replyJson.has("type")
                        ) {
                            ReplyMessage(
                                id = replyJson.getString("_id"),
                                senderId = replyJson.getString("senderId"),
                                senderName = replyJson.optString("senderName", null),
                                content = replyJson.getString("content"),
                                type = replyJson.getString("type"),
                                mediaUrl = replyJson.optString("mediaUrl", null).takeIf { it != "null" }
                            )
                        } else null
                    } else null
                    
                    val message = Message(
                        id = messageJson.getString("_id"),
                        tempId = tempId,
                        conversationId = messageJson.getString("conversationId"),
                        senderId = messageJson.getString("senderId"),
                        receiverId = messageJson.getString("receiverId"),
                        senderName = messageJson.optString("senderName", null),
                        receiverName = messageJson.optString("receiverName", null),
                        senderPhoto = messageJson.optString("senderPhoto", null).takeIf { it != "null" },
                        receiverPhoto = messageJson.optString("receiverPhoto", null).takeIf { it != "null" },
                        content = messageJson.getString("content"),
                        type = messageJson.getString("type"),
                        mediaUrl = messageJson.optString("mediaUrl", null).takeIf { it != "null" },
                        status = messageJson.getString("status"),
                        failReason = messageJson.optString("failReason", null).takeIf { it != "null" },
                        replyTo = replyTo,
                        giftId = messageJson.optString("giftId", null).takeIf { it != "null" },
                        createdAt = messageJson.getString("createdAt"),
                        updatedAt = messageJson.optString("updatedAt", null).takeIf { it != "null" }
                    )
                    
                    val status = response.getString("status")
                    val conversationId = response.getString("conversationId")
                    
                    callback(SendMessageResponse.Success(message, status, conversationId))
                } else {
                    val error = response.getString("error")
                    val failReason = response.optString("failReason", "unknown")
                    val currentBalance = response.optInt("currentBalance", 0)
                    val requiredTokens = response.optInt("requiredTokens", 0)
                    
                    callback(SendMessageResponse.Failed(
                        error = error,
                        failReason = failReason,
                        currentBalance = currentBalance,
                        requiredTokens = requiredTokens
                    ))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Send message response parse hatası", e)
                callback(SendMessageResponse.Error(e.message ?: "Unknown error"))
            }
        })
        
        Log.d(TAG, "📤 Mesaj gönderiliyor: $tempId")
    }
    
    /**
     * Mesaj teslim edildi
     */
    fun markAsDelivered(messageId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
        }
        socket?.emit("message:delivered", data)
    }
    
    /**
     * Mesaj okundu
     */
    fun markAsRead(messageId: String) {
        val data = JSONObject().apply {
            put("messageId", messageId)
        }
        socket?.emit("message:read", data)
    }
    
    /**
     * Konuşmayı okundu işaretle
     */
    fun markConversationAsRead(conversationId: String) {
        val data = JSONObject().apply {
            put("conversationId", conversationId)
        }
        socket?.emit("conversation:markRead", data)
    }
    
    /**
     * Yazmaya başladı
     */
    fun startTyping(receiverId: String) {
        val data = JSONObject().apply {
            put("receiverId", receiverId)
        }
        socket?.emit("typing:start", data)
    }
    
    /**
     * Yazmayı bıraktı
     */
    fun stopTyping(receiverId: String) {
        val data = JSONObject().apply {
            put("receiverId", receiverId)
        }
        socket?.emit("typing:stop", data)
    }
    
    /**
     * Bağlantıyı kes
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        _isConnected.value = false
        currentUserId = null
        Log.d(TAG, "Socket bağlantısı kapatıldı")
    }
    
    /**
     * Bağlantı durumu
     */
    fun isConnected(): Boolean = socket?.connected() == true
}

