# Socket.IO Entegrasyonu

Matcher Android uygulamasının gerçek zamanlı mesajlaşma sistemi için Socket.IO entegrasyonu.

> **📱 Toast Sistemi**: Socket bağlantı durumları ve mesaj bildirimleri için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 🌐 Socket Bağlantısı

### Server URL
```
https://admin54.askologapp.com:3001
```

### Bağlantı Konfigürasyonu
```kotlin
val options = IO.Options().apply {
    auth = mapOf("token" to authToken)
    transports = arrayOf("websocket", "polling")
    reconnection = true
    reconnectionDelay = 1000
    reconnectionDelayMax = 5000
    reconnectionAttempts = 5
    secure = true
}
```

## 🔌 SocketManager Sınıfı

### Singleton Pattern
```kotlin
class SocketManager private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: SocketManager? = null
        
        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }
}
```

### Bağlantı Durumu
```kotlin
// Connection state
private val _isConnected = MutableStateFlow(false)
val isConnected: StateFlow<Boolean> = _isConnected
```

## 📡 Event'ler

### 1. Bağlantı Event'leri

#### `connect` - Bağlantı Başarılı
```kotlin
on(Socket.EVENT_CONNECT) {
    Log.d(TAG, "✅ Socket bağlantısı başarılı")
    _isConnected.value = true
}
```

#### `disconnect` - Bağlantı Koptu
```kotlin
on(Socket.EVENT_DISCONNECT) {
    Log.d(TAG, "❌ Socket bağlantısı koptu")
    _isConnected.value = false
}
```

#### `connect_error` - Bağlantı Hatası
```kotlin
on(Socket.EVENT_CONNECT_ERROR) { args ->
    val error = args.firstOrNull()
    Log.e(TAG, "❌ Bağlantı hatası: $error")
    _isConnected.value = false
}
```

#### `reconnect` - Yeniden Bağlandı
```kotlin
on("reconnect") {
    Log.d(TAG, "🔄 Yeniden bağlanıldı")
    _isConnected.value = true
}
```

### 2. Mesaj Event'leri

#### `message:receive` - Yeni Mesaj Geldi
**Gelen Veri:**
```json
{
  "message": {
    "_id": "message_id",
    "conversationId": "conversation_id",
    "senderId": "sender_user_id",
    "receiverId": "receiver_user_id",
    "senderName": "Gönderen Adı",
    "receiverName": "Alıcı Adı",
    "senderPhoto": "sender_photo_url",
    "receiverPhoto": "receiver_photo_url",
    "content": "Mesaj içeriği",
    "type": "text|image|video|gift",
    "mediaUrl": "media_url",
    "status": "sent|delivered|read",
    "failReason": "Hata sebebi",
    "deliveredAt": "2024-01-01T00:00:00Z",
    "readAt": "2024-01-01T00:00:00Z",
    "replyTo": {
      "_id": "reply_message_id",
      "senderId": "reply_sender_id",
      "senderName": "Reply Gönderen",
      "content": "Reply mesaj içeriği",
      "type": "text|image|video",
      "mediaUrl": "reply_media_url"
    },
    "giftId": "gift_id",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  },
  "conversationId": "conversation_id"
}
```

**Kullanım:**
```kotlin
on("message:receive") { args ->
    try {
        val data = args[0] as JSONObject
        val messageJson = data.getJSONObject("message")
        val conversationId = data.getString("conversationId")
        
        val message = Message(
            id = messageJson.getString("_id"),
            conversationId = messageJson.getString("conversationId"),
            senderId = messageJson.getString("senderId"),
            receiverId = messageJson.getString("receiverId"),
            senderName = messageJson.optString("senderName", null),
            receiverName = messageJson.optString("receiverName", null),
            senderPhoto = messageJson.optString("senderPhoto", null),
            receiverPhoto = messageJson.optString("receiverPhoto", null),
            content = messageJson.getString("content"),
            type = messageJson.getString("type"),
            mediaUrl = messageJson.optString("mediaUrl", null),
            status = messageJson.getString("status"),
            failReason = messageJson.optString("failReason", null),
            deliveredAt = messageJson.optString("deliveredAt", null),
            readAt = messageJson.optString("readAt", null),
            replyTo = parseReplyMessage(messageJson),
            giftId = messageJson.optString("giftId", null),
            createdAt = messageJson.getString("createdAt"),
            updatedAt = messageJson.optString("updatedAt", null)
        )
        
        _incomingMessages.value = SocketMessageEvent(message, conversationId)
    } catch (e: Exception) {
        Log.e(TAG, "Mesaj parse hatası", e)
    }
}
```

#### `message:status` - Mesaj Durumu Güncellendi
**Gelen Veri:**
```json
{
  "messageId": "message_id",
  "status": "sent|delivered|read|failed",
  "tempId": "temporary_message_id",
  "deliveredAt": "2024-01-01T00:00:00Z",
  "readAt": "2024-01-01T00:00:00Z",
  "failReason": "Hata sebebi"
}
```

**Kullanım:**
```kotlin
on("message:status") { args ->
    try {
        val data = args[0] as JSONObject
        val update = MessageStatusUpdate(
            messageId = data.getString("messageId"),
            status = data.getString("status"),
            tempId = data.optString("tempId", null),
            deliveredAt = data.optString("deliveredAt", null),
            readAt = data.optString("readAt", null),
            failReason = data.optString("failReason", null)
        )
        
        _messageStatusUpdates.value = update
    } catch (e: Exception) {
        Log.e(TAG, "Status parse hatası", e)
    }
}
```

#### `conversation:read` - Konuşma Okundu
**Gelen Veri:**
```json
{
  "conversationId": "conversation_id",
  "readBy": "user_id"
}
```

**Kullanım:**
```kotlin
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
```

### 3. Typing Event'leri

#### `typing:status` - Yazıyor Durumu
**Gelen Veri:**
```json
{
  "userId": "user_id",
  "isTyping": true|false
}
```

**Kullanım:**
```kotlin
on("typing:status") { args ->
    try {
        val data = args[0] as JSONObject
        val status = TypingStatus(
            userId = data.getString("userId"),
            isTyping = data.getBoolean("isTyping")
        )
        
        _typingStatusUpdates.value = status
    } catch (e: Exception) {
        Log.e(TAG, "Typing parse hatası", e)
    }
}
```

### 4. Online/Offline Event'leri

#### `user:online` - Kullanıcı Online
**Gelen Veri:**
```json
{
  "userId": "user_id"
}
```

**Kullanım:**
```kotlin
on("user:online") { args ->
    try {
        val data = args[0] as JSONObject
        _onlineStatusUpdates.value = UserStatus(
            userId = data.getString("userId"),
            isOnline = true
        )
    } catch (e: Exception) {
        Log.e(TAG, "User online parse hatası", e)
    }
}
```

#### `user:offline` - Kullanıcı Offline
**Gelen Veri:**
```json
{
  "userId": "user_id"
}
```

**Kullanım:**
```kotlin
on("user:offline") { args ->
    try {
        val data = args[0] as JSONObject
        _onlineStatusUpdates.value = UserStatus(
            userId = data.getString("userId"),
            isOnline = false
        )
    } catch (e: Exception) {
        Log.e(TAG, "User offline parse hatası", e)
    }
}
```

## 📤 Gönderilen Event'ler

### 1. Mesaj Gönderme

#### `message:send` - Mesaj Gönder
**Gönderilen Veri:**
```json
{
  "receiverId": "receiver_user_id",
  "content": "Mesaj içeriği",
  "type": "text|image|video|gift",
  "mediaUrl": "media_url",
  "tempId": "temporary_message_id",
  "resendMessageId": "resend_message_id",
  "replyTo": "reply_message_id",
  "giftId": "gift_id"
}
```

**Kullanım:**
```kotlin
fun sendMessage(
    receiverId: String,
    content: String,
    type: String = "text",
    mediaUrl: String? = null,
    tempId: String = UUID.randomUUID().toString(),
    resendMessageId: String? = null,
    replyTo: ReplyMessage? = null,
    giftId: String? = null,
    callback: (SendMessageResponse) -> Unit
) {
    val data = JSONObject().apply {
        put("receiverId", receiverId)
        put("content", content)
        put("type", type)
        put("tempId", tempId)
        if (mediaUrl != null) put("mediaUrl", mediaUrl)
        if (resendMessageId != null) put("resendMessageId", resendMessageId)
        if (replyTo != null) put("replyTo", replyTo.id)
        if (giftId != null) put("giftId", giftId)
    }
    
    socket?.emit("message:send", data, Ack { args ->
        // Response handling
    })
}
```

**Response:**
```kotlin
sealed class SendMessageResponse {
    data class Success(
        val message: Message,
        val status: String,
        val conversationId: String
    ) : SendMessageResponse()
    
    data class Failed(
        val error: String,
        val failReason: String,
        val currentBalance: Int,
        val requiredTokens: Int
    ) : SendMessageResponse()
    
    data class Error(
        val message: String
    ) : SendMessageResponse()
}
```

### 2. Mesaj Durumu Güncellemeleri

#### `message:delivered` - Mesaj Teslim Edildi
```kotlin
fun markAsDelivered(messageId: String) {
    val data = JSONObject().apply {
        put("messageId", messageId)
    }
    socket?.emit("message:delivered", data)
}
```

#### `message:read` - Mesaj Okundu
```kotlin
fun markAsRead(messageId: String) {
    val data = JSONObject().apply {
        put("messageId", messageId)
    }
    socket?.emit("message:read", data)
}
```

#### `conversation:markRead` - Konuşma Okundu İşaretle
```kotlin
fun markConversationAsRead(conversationId: String) {
    val data = JSONObject().apply {
        put("conversationId", conversationId)
    }
    socket?.emit("conversation:markRead", data)
}
```

### 3. Typing Event'leri

#### `typing:start` - Yazmaya Başladı
```kotlin
fun startTyping(receiverId: String) {
    val data = JSONObject().apply {
        put("receiverId", receiverId)
    }
    socket?.emit("typing:start", data)
}
```

#### `typing:stop` - Yazmayı Bıraktı
```kotlin
fun stopTyping(receiverId: String) {
    val data = JSONObject().apply {
        put("receiverId", receiverId)
    }
    socket?.emit("typing:stop", data)
}
```

## 📊 StateFlow Yönetimi

### Incoming Messages
```kotlin
private val _incomingMessages = MutableStateFlow<SocketMessageEvent?>(null)
val incomingMessages: StateFlow<SocketMessageEvent?> = _incomingMessages
```

### Message Status Updates
```kotlin
private val _messageStatusUpdates = MutableStateFlow<MessageStatusUpdate?>(null)
val messageStatusUpdates: StateFlow<MessageStatusUpdate?> = _messageStatusUpdates
```

### Typing Status
```kotlin
private val _typingStatusUpdates = MutableStateFlow<TypingStatus?>(null)
val typingStatusUpdates: StateFlow<TypingStatus?> = _typingStatusUpdates
```

### Online Status
```kotlin
private val _onlineStatusUpdates = MutableStateFlow<UserStatus?>(null)
val onlineStatusUpdates: StateFlow<UserStatus?> = _onlineStatusUpdates
```

## 🎯 Compose'da Kullanım

### Mesaj Dinleme
```kotlin
@Composable
fun ChatScreen(conversationId: String) {
    val socketManager = remember { SocketManager.getInstance() }
    val incomingMessages by socketManager.incomingMessages.collectAsState()
    
    LaunchedEffect(incomingMessages) {
        incomingMessages?.let { event ->
            if (event.conversationId == conversationId) {
                // Yeni mesajı listeye ekle
                addMessageToList(event.message)
            }
        }
    }
}
```

### Mesaj Durumu Güncelleme
```kotlin
@Composable
fun MessageItem(message: Message) {
    val socketManager = remember { SocketManager.getInstance() }
    val statusUpdates by socketManager.messageStatusUpdates.collectAsState()
    
    LaunchedEffect(statusUpdates) {
        statusUpdates?.let { update ->
            if (update.messageId == message.id) {
                // Mesaj durumunu güncelle
                updateMessageStatus(update)
            }
        }
    }
}
```

### Typing Indicator
```kotlin
@Composable
fun TypingIndicator(receiverId: String) {
    val socketManager = remember { SocketManager.getInstance() }
    val typingStatus by socketManager.typingStatusUpdates.collectAsState()
    
    val isTyping = typingStatus?.userId == receiverId && typingStatus?.isTyping == true
    
    if (isTyping) {
        Text("Yazıyor...")
    }
}
```

### Online Status
```kotlin
@Composable
fun UserStatusIndicator(userId: String) {
    val socketManager = remember { SocketManager.getInstance() }
    val onlineStatus by socketManager.onlineStatusUpdates.collectAsState()
    
    val isOnline = onlineStatus?.userId == userId && onlineStatus?.isOnline == true
    
    Box {
        if (isOnline) {
            Circle(color = Color.Green, radius = 4.dp)
        }
    }
}
```

## 🔧 Bağlantı Yönetimi

### Bağlantı Başlatma
```kotlin
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
    } catch (e: Exception) {
        Log.e(TAG, "Socket bağlantı hatası", e)
    }
}
```

### Bağlantı Kesme
```kotlin
fun disconnect() {
    socket?.disconnect()
    socket?.off()
    socket = null
    _isConnected.value = false
    currentUserId = null
    Log.d(TAG, "Socket bağlantısı kapatıldı")
}
```

### Bağlantı Durumu Kontrolü
```kotlin
fun isConnected(): Boolean = socket?.connected() == true
```

## 📱 Mesaj Tipleri

### Text Mesajı
```kotlin
socketManager.sendMessage(
    receiverId = "user_id",
    content = "Merhaba!",
    type = "text"
) { response ->
    when (response) {
        is SendMessageResponse.Success -> {
            // Mesaj başarıyla gönderildi
        }
        is SendMessageResponse.Failed -> {
            // Mesaj gönderilemedi (token yetersiz vs.)
        }
        is SendMessageResponse.Error -> {
            // Hata oluştu
        }
    }
}
```

### Resim Mesajı
```kotlin
socketManager.sendMessage(
    receiverId = "user_id",
    content = "Resim mesajı",
    type = "image",
    mediaUrl = "https://example.com/image.jpg"
) { response ->
    // Response handling
}
```

### Video Mesajı
```kotlin
socketManager.sendMessage(
    receiverId = "user_id",
    content = "Video mesajı",
    type = "video",
    mediaUrl = "https://example.com/video.mp4"
) { response ->
    // Response handling
}
```

### Hediye Mesajı
```kotlin
socketManager.sendMessage(
    receiverId = "user_id",
    content = "Hediye gönderildi",
    type = "gift",
    giftId = "gift_id"
) { response ->
    // Response handling
}
```

### Reply Mesajı
```kotlin
val replyMessage = ReplyMessage(
    id = "original_message_id",
    senderId = "original_sender_id",
    senderName = "Original Sender",
    content = "Orijinal mesaj",
    type = "text"
)

socketManager.sendMessage(
    receiverId = "user_id",
    content = "Reply mesajı",
    type = "text",
    replyTo = replyMessage
) { response ->
    // Response handling
}
```

## 🚨 Hata Yönetimi

### Bağlantı Hataları
```kotlin
on(Socket.EVENT_CONNECT_ERROR) { args ->
    val error = args.firstOrNull()
    Log.e(TAG, "❌ Bağlantı hatası: $error")
    _isConnected.value = false
    
    // Kullanıcıya hata bildir
    showErrorToast("Bağlantı hatası: $error")
}
```

### Mesaj Gönderme Hataları
```kotlin
when (response) {
    is SendMessageResponse.Failed -> {
        when (response.failReason) {
            "insufficient_tokens" -> {
                showErrorToast("Yetersiz token. Mevcut: ${response.currentBalance}, Gerekli: ${response.requiredTokens}")
            }
            "user_blocked" -> {
                showErrorToast("Kullanıcı tarafından engellenmişsiniz")
            }
            "conversation_not_started" -> {
                showErrorToast("Konuşma başlatılmamış")
            }
            else -> {
                showErrorToast("Mesaj gönderilemedi: ${response.error}")
            }
        }
    }
    is SendMessageResponse.Error -> {
        showErrorToast("Hata: ${response.message}")
    }
}
```

## 🔄 Yeniden Bağlanma

Socket.IO otomatik olarak yeniden bağlanma özelliği ile gelir:

```kotlin
val options = IO.Options().apply {
    reconnection = true
    reconnectionDelay = 1000
    reconnectionDelayMax = 5000
    reconnectionAttempts = 5
}
```

Bu ayarlar:
- **reconnection**: Yeniden bağlanmayı etkinleştirir
- **reconnectionDelay**: İlk yeniden bağlanma gecikmesi (1 saniye)
- **reconnectionDelayMax**: Maksimum yeniden bağlanma gecikmesi (5 saniye)
- **reconnectionAttempts**: Maksimum yeniden bağlanma denemesi (5 kez)

Bu Socket.IO entegrasyonu, uygulamanın gerçek zamanlı mesajlaşma özelliklerini tam olarak destekler.
