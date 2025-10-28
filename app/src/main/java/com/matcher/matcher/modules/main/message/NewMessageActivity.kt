package com.matcher.matcher.modules.main.message

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.app.Activity
import androidx.activity.ComponentActivity
import com.matcher.matcher.utils.helpers.ToastHelper
import com.matcher.matcher.network.model.ApiException
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.matcher.matcher.R
import com.matcher.matcher.models.message.core.Message
import com.matcher.matcher.models.message.core.ReplyMessage
import com.matcher.matcher.models.message.SendMessageResponse
import com.matcher.matcher.models.ticket.StartMessage
import com.matcher.matcher.models.user.User
import com.matcher.matcher.components.ImagePickerBottomSheet
import com.matcher.matcher.components.GiftPickerBottomSheet
import com.airbnb.lottie.compose.*
import com.matcher.matcher.modules.main.components.MessageInputView
import com.matcher.matcher.modules.main.components.NewMessageScreen
import com.matcher.matcher.modules.main.profile.ReportActivity
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.network.service.FileUploadService
import com.matcher.matcher.models.user.request.BlockRequest
import com.matcher.matcher.network.socket.SocketManager
import com.matcher.matcher.models.gift.Gift
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.matcher.matcher.models.message.socket.MessageStatusUpdate
import com.matcher.matcher.models.message.socket.SocketMessageEvent

class NewMessageActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "NewMessageActivity"
        
        /**
         * Kullanıcı ile mesajlaşma başlat
         * Önce conversation listesinde ara, varsa aç, yoksa yeni mesaj ekranı aç
         */
        fun startMessaging(
            context: Context,
            otherUserId: String,
            otherUserName: String,
            otherUserPhotoUrl: String?
        ) {
            // Conversation listesinde bu kullanıcı ile olan konuşmayı ara
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.matcher.matcher.network.ApiClient.getInstance(context)
                            .messageService
                            .getConversations()
                    }
                    
                    if (response.success) {
                        // Bu kullanıcı ile olan conversation'ı bul
                        val existingConversation = response.conversations.find { conversation ->
                            conversation.otherUser?.id == otherUserId
                        }
                        
                        if (existingConversation != null) {
                            // Mevcut conversation varsa onu aç
                            openConversation(
                                context = context,
                                conversationId = existingConversation.id,
                                otherUserId = otherUserId,
                                otherUserName = otherUserName,
                                otherUserPhotoUrl = otherUserPhotoUrl
                            )
                        } else {
                            // Conversation yoksa yeni mesaj ekranı aç (conversation ID olmadan)
                            startNewChat(
                                context = context,
                                otherUserId = otherUserId,
                                otherUserName = otherUserName,
                                otherUserPhotoUrl = otherUserPhotoUrl
                            )
                        }
                    } else {
                        // API hatası, yine de yeni mesaj ekranı aç
                        startNewChat(
                            context = context,
                            otherUserId = otherUserId,
                            otherUserName = otherUserName,
                            otherUserPhotoUrl = otherUserPhotoUrl
                        )
                    }
                } catch (e: Exception) {
                    // Hata durumunda yine de mesaj ekranı aç
                    startNewChat(
                        context = context,
                        otherUserId = otherUserId,
                        otherUserName = otherUserName,
                        otherUserPhotoUrl = otherUserPhotoUrl
                    )
                }
            }
        }
        
        // Yeni chat başlat (conversation ID olmadan)
        private fun startNewChat(
            context: Context,
            otherUserId: String,
            otherUserName: String,
            otherUserPhotoUrl: String?
        ) {
            val intent = Intent(context, NewMessageActivity::class.java).apply {
                // conversationId yok - yeni conversation
                putExtra("otherUserId", otherUserId)
                putExtra("otherUserName", otherUserName)
                putExtra("otherUserPhotoUrl", otherUserPhotoUrl)
            }
            context.startActivity(intent)
        }
        
        // Yeni conversation başlatmak için (deprecated - startMessaging kullan)
        @Deprecated("Use startMessaging instead", ReplaceWith("startMessaging(context, otherUserId, otherUserName, otherUserPhotoUrl)"))
        fun startNewConversation(
            context: Context,
            otherUserId: String,
            otherUserName: String,
            otherUserPhotoUrl: String?
        ) {
            startMessaging(context, otherUserId, otherUserName, otherUserPhotoUrl)
        }
        
        // Mevcut conversation'ı açmak için
        fun openConversation(
            context: Context,
            conversationId: String,
            otherUserId: String,
            otherUserName: String,
            otherUserPhotoUrl: String?
        ) {
            val intent = Intent(context, NewMessageActivity::class.java).apply {
                putExtra("conversationId", conversationId)
                putExtra("otherUserId", otherUserId)
                putExtra("otherUserName", otherUserName)
                putExtra("otherUserPhotoUrl", otherUserPhotoUrl)
            }
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Intent'ten verileri al
        val conversationId = intent.getStringExtra("conversationId") // null ise yeni conversation
        val otherUserId = intent.getStringExtra("otherUserId") ?: ""
        val otherUserName = intent.getStringExtra("otherUserName") ?: "Kullanıcı"
        val otherUserPhotoUrl = intent.getStringExtra("otherUserPhotoUrl")
        
        setContent {
            NewMessageScreen(
                conversationId = conversationId,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                otherUserPhotoUrl = otherUserPhotoUrl,
                onBack = { finish() }
            )
        }
    }
}


/**
 * Chat içeriği - mesajları listeleyen ana component
 */
@Composable
fun ChatContent(
    conversationId: String?,
    otherUserId: String,
    otherUserName: String,
    refreshTrigger: Int = 0,
    onResendMessage: ((Message) -> Unit)? = null,
    onMessagesEmpty: (() -> Unit)? = null,
    onReplyMessage: ((Message) -> Unit)? = null,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val socketManager = SocketManager.getInstance()
    
    // State'ler
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasMore by remember { mutableStateOf(true) }
    var currentPage by remember { mutableStateOf(1) }
    
    // Mesajları yükle
    fun loadMessages(page: Int = 1, append: Boolean = false) {
        val convId = conversationId ?: return
        
        coroutineScope.launch {
            try {
                isLoading = true
                errorMessage = null
                
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getInstance(context).messageService.getMessages(
                        conversationId = convId,
                        page = page,
                        limit = 30
                    )
                }
                
                if (response.success) {
                    if (append) {
                        // Eski mesajları ekle (sayfa yükleme)
                        messages = messages + response.messages
                    } else {
                        // API'den gelen mesajları olduğu gibi kullan (zaten sıralı)
                        messages = response.messages
                    }
                    hasMore = response.hasMore
                    currentPage = page
                    
                } else {
                    errorMessage = "Mesajlar yüklenemedi"
                }
            } catch (e: Exception) {
                errorMessage = "Hata: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // İlk yükleme ve refresh
    LaunchedEffect(conversationId, refreshTrigger) {
        if (conversationId != null) {
            loadMessages(page = 1, append = false)
            
            // Konuşmayı okundu işaretle
            socketManager.markConversationAsRead(conversationId)

            // Mesajlar yüklendikten sonra scroll yap
            delay(300) // Mesajların yüklenmesi için gecikme
            listState.animateScrollToItem(0)
        }
    }
    
    // Socket'ten yeni mesajları dinle
    LaunchedEffect(Unit) {
        launch {
            socketManager.incomingMessages.collect { event: SocketMessageEvent? ->
                event?.let { socketEvent: SocketMessageEvent ->
                    // Bu conversation'a ait mesaj mı?
                    if (socketEvent.conversationId == conversationId) {
                        // Eğer mesaj zaten listede yoksa ekle
                        if (messages.none { msg: Message -> msg.id == socketEvent.message.id }) {
                            // Mesajı doğru yere ekle (tarih sırasına göre)
                            val newMessage = socketEvent.message
                            val newMessageTime = try {
                                Instant.parse(newMessage.createdAt).toEpochMilli()
                            } catch (e: Exception) {
                                0L
                            }
                            
                            // Mesajı doğru yere ekle
                            val insertIndex = messages.indexOfFirst { existingMsg ->
                                try {
                                    val existingTime = Instant.parse(existingMsg.createdAt).toEpochMilli()
                                    newMessageTime < existingTime
                                } catch (e: Exception) {
                                    false
                                }
                            }
                            
                            if (insertIndex == -1) {
                                // En yeni mesaj, listenin başına ekle
                                messages = listOf(newMessage) + messages
                            } else {
                                // Doğru yere ekle
                                messages = messages.toMutableList().apply {
                                    add(insertIndex, newMessage)
                                }
                            }
                            

                            // Yeni gelen mesajı okundu işaretle (eğer karşı taraftan geldiyse)
                            if (socketEvent.message.receiverId == User.current?._id) {
                                socketEvent.message.id?.let { messageId: String ->
                                    socketManager.markAsRead(messageId)
                                }
                            }
                            
                            // En alta scroll
                            coroutineScope.launch {
                                delay(100) // Mesajın UI'da render edilmesi için gecikme
                                listState.animateScrollToItem(0)
                            }
                        }
                    }
                }
            }
        }
        
        // Mesaj durumu güncellemelerini dinle
        launch {
            socketManager.messageStatusUpdates.collect { update: MessageStatusUpdate? ->
                update?.let { statusUpdate: MessageStatusUpdate ->
                    // Mesajın durumunu güncelle
                    messages = messages.map { msg: Message ->
                        if (msg.id == statusUpdate.messageId || msg.tempId == statusUpdate.tempId) {
                            msg.copy(
                                status = statusUpdate.status,
                                failReason = statusUpdate.failReason,
                                deliveredAt = statusUpdate.deliveredAt,
                                readAt = statusUpdate.readAt
                            )
                        } else {
                            msg
                        }
                    }
                }
            }
        }
    }
    
    Box(modifier = modifier) {
        if (conversationId == null) {
            // Henüz conversation yok
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.home_send_message),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Konuşmaya başlamak için\nbir mesaj gönderin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (isLoading && messages.isEmpty()) {
            // İlk yükleme loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.primaryColor)
                )
            }
        } else if (errorMessage != null && messages.isEmpty()) {
            // Hata durumu
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Bir hata oluştu",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { loadMessages() }) {
                        Text("Tekrar Dene")
                    }
                }
            }
        } else if (messages.isEmpty()) {
            // Mesaj yok
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Henüz mesaj yok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            // Mesajları göster
            val currentUserId = User.current?._id ?: ""
            val groupedMessages = groupMessagesByDate(messages)
            val sortedDates = groupedMessages.keys.sortedDescending() // En yeni tarih en üstte
            
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true // En yeni mesaj en altta
            ) {
                // Her tarih grubu için
                sortedDates.forEach { dateKey ->
                    val messagesForDate = groupedMessages[dateKey] ?: emptyList()
                    
                    // Mesajları tarih sırasına göre sırala (en yeni en altta)
                    val sortedMessagesForDate = messagesForDate.sortedByDescending { msg ->
                        try {
                            Instant.parse(msg.createdAt).toEpochMilli()
                        } catch (e: Exception) {
                            0L
                        }
                    }
                    
                    items(
                        items = sortedMessagesForDate,
                        key = { msg -> msg.id ?: msg.tempId ?: msg.createdAt }
                    ) { message ->
                        ChatMessageItem(
                            message = message,
                            isMine = message.isMine(currentUserId),
                            onImageClick = { imageUrl ->
                                // TODO: Resmi tam ekran göster
                            },
                            onResendClick = { failedMessage ->
                                onResendMessage?.invoke(failedMessage)
                            },
                            onReplyClick = { messageToReply ->
                                onReplyMessage?.invoke(messageToReply)
                            },
                            onDeleteClick = { messageToDelete ->
                                // Mesajı sil
                                val messageId = messageToDelete.id
                                if (messageId != null) {
                                    coroutineScope.launch {
                                        try {

                                            val response = withContext(Dispatchers.IO) {
                                                ApiClient.getInstance(context)
                                                    .messageService
                                                    .deleteMessage(messageId)
                                            }
                                            
                                            if (response.success) {
                                                // Mesajı listeden çıkar
                                                messages = messages.filterNot { it.id == messageId }

                                                // Eğer tüm mesajlar silindiyse, parent'a bildir
                                                if (messages.isEmpty()) {
                                                    onMessagesEmpty?.invoke()
                                                }
                                                
                                                (context as? Activity)?.let { activity ->
                                                    ToastHelper.showSuccess(activity, "Mesaj silindi")
                                                }
                                            } else {
                                                (context as? Activity)?.let { activity ->
                                                    ToastHelper.showError(activity, response.message)
                                                }
                                            }
                                        } catch (e: Exception) {
                                            val errorMessage = ApiException.getErrorMessage(e)
                                            (context as? Activity)?.let { activity ->
                                                ToastHelper.showError(activity, errorMessage)
                                            }
                                        }
                                    }
                                } else {
                                    (context as? Activity)?.let { activity ->
                                        ToastHelper.showWarning(activity, "Mesaj ID'si bulunamadı")
                                    }
                                }
                            },
                            onReportClick = { messageToReport ->
                                // Mesajı gönderen kullanıcıyı şikayet et
                                ReportActivity.start(context, otherUserId, otherUserName)
                            }
                        )
                    }
                    
                    // Tarih header'ı
                    item(key = "header_$dateKey") {
                        ChatDateHeader(date = dateKey)
                    }
                }
                
                // Load more için
                if (hasMore && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(onClick = { loadMessages(page = currentPage + 1, append = true) }) {
                                Text("Daha Fazla Yükle")
                            }
                        }
                    }
                }
                
                if (isLoading && messages.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = colorResource(id = R.color.primaryColor)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TemplateMessagesView(
    profileName: String,
    onTemplateClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State'ler
    var startMessages by remember { mutableStateOf<List<StartMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // API'den başlangıç mesajlarını çek
    LaunchedEffect(Unit) {
        try {
            val ticketService = ApiClient.getInstance(context).ticketService
            val response = ticketService.getStartMessages()
            if (response.success && response.data != null) {
                startMessages = response.data
            }
            // Hata durumunda sessizce devam et - kullanıcı normal mesaj yazabilir
        } catch (e: Exception) {
            // Template mesajlar kritik değil, hata gösterme
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "Hadi selam ver 👋",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp),
            textAlign = TextAlign.Center
        )
        
        if (isLoading) {
            // Loading indicator
            CircularProgressIndicator(
                color = colorResource(id = R.color.primaryColor),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // API'den gelen mesajları göster
            startMessages.forEach { startMessage ->
                // Profil ismini mesaja inject et
                val messageWithName = startMessage.message.replace("{profileName}", profileName)
                
                Surface(
                    onClick = { onTemplateClick(messageWithName) },
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colorResource(id = R.color.primaryDarkColor)
                ) {
                    Text(
                        text = messageWithName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
