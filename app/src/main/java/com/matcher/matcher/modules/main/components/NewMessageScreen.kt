package com.matcher.matcher.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.airbnb.lottie.compose.*
import com.matcher.matcher.R
import com.matcher.matcher.components.ImagePickerBottomSheet
import com.matcher.matcher.components.GiftPickerBottomSheet
import com.matcher.matcher.models.gift.Gift
import com.matcher.matcher.models.message.core.Message
import com.matcher.matcher.models.message.core.ReplyMessage
import com.matcher.matcher.models.message.socket.MessageStatusUpdate
import com.matcher.matcher.models.message.socket.SocketMessageEvent
import com.matcher.matcher.models.user.User
import com.matcher.matcher.models.user.request.BlockRequest
import com.matcher.matcher.modules.main.message.ChatContent
import com.matcher.matcher.modules.main.message.ChatDateHeader
import com.matcher.matcher.modules.main.message.ChatMessageItem
import com.matcher.matcher.modules.main.message.TemplateMessagesView
import com.matcher.matcher.modules.main.profile.ReportActivity
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.network.model.ApiException
import com.matcher.matcher.network.service.FileUploadService
import com.matcher.matcher.network.socket.SocketManager
import com.matcher.matcher.models.message.SendMessageResponse
import com.matcher.matcher.utils.helpers.ToastHelper
import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

@Composable
fun NewMessageScreen(
    conversationId: String?,
    otherUserId: String,
    otherUserName: String,
    otherUserPhotoUrl: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val socketManager = SocketManager.getInstance()
    val listState = rememberLazyListState() // Chat scroll state'i
    
    var message by remember { mutableStateOf("") }
    var currentConversationId by remember { mutableStateOf(conversationId) }
    var isSending by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) } // Chat'i refresh etmek için
    var isBlocked by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var replyMessage by remember { mutableStateOf<ReplyMessage?>(null) } // Reply edilen mesaj
    var showGiftPicker by remember { mutableStateOf(false) }
    
    var hasMessages by remember { mutableStateOf(conversationId != null) }
    var isCheckingMessages by remember { mutableStateOf(conversationId != null) }
    
    LaunchedEffect(otherUserId) {
        try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getInstance(context).userService.checkBlockStatus(otherUserId)
            }
            if (response.success && response.data != null) {
                isBlocked = response.data.isBlocked
            }
        } catch (e: Exception) {
        }
    }
    
    fun sendMessage(messageContent: String, resendMessageId: String? = null) {
        if (messageContent.isBlank() || isSending) return
        
        isSending = true
        val tempId = java.util.UUID.randomUUID().toString()
        
        if (resendMessageId != null) {
        } else {
        }
        
        socketManager.sendMessage(
            receiverId = otherUserId,
            content = messageContent.trim(),
            type = "text",
            tempId = tempId,
            resendMessageId = resendMessageId, // Tekrar gönderilen mesajın ID'si
            replyTo = replyMessage // Reply edilen mesaj
        ) { response ->
            coroutineScope.launch {
                isSending = false
                when (response) {
                    is SendMessageResponse.Success -> {
                        // Conversation ID'yi sakla
                        if (currentConversationId == null) {
                            currentConversationId = response.conversationId
                        }
                        // Artık mesaj var, template'leri gizle
                        hasMessages = true
                        message = ""
                        replyMessage = null // Reply state'ini temizle
                        // Chat'i refresh et (tekrar gönderimde de refresh gerekli)
                        refreshTrigger++
                    }
                    is SendMessageResponse.Failed -> {
                        // Sadece yetersiz jeton durumunda toast göster
                        if (response.failReason == "insufficient_tokens") {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showWarning(
                                    activity,
                                    "Yetersiz jeton! Gerekli: ${response.requiredTokens}, Mevcut: ${response.currentBalance}"
                                )
                            }
                        }
                    }
                    is SendMessageResponse.Error -> {
                    }
                }
            }
        }
    }
    
    // Hediye mesajı gönderme fonksiyonu
    fun sendGiftMessage(gift: Gift) {
        if (isSending) return
        
        isSending = true
        val tempId = java.util.UUID.randomUUID().toString()
        socketManager.sendMessage(
            receiverId = otherUserId,
            content = "🎁 ${gift.name}", // API için minimum content gerekiyor
            type = "image", // Hediye mesajları resim tipinde olacak
            mediaUrl = gift.image, // Hediye resmi
            tempId = tempId,
            giftId = gift.id // Hediye ID'si
        ) { response ->
            coroutineScope.launch {
                isSending = false
                
                when (response) {
                    is SendMessageResponse.Success -> {
                        // Conversation ID'yi sakla
                        if (currentConversationId == null) {
                            currentConversationId = response.conversationId
                        }
                        // Artık mesaj var, template'leri gizle
                        hasMessages = true
                        // Chat'i refresh et
                        refreshTrigger++
                        
                        // Hediye gönderildi - toast gösterme
                    }
                    is SendMessageResponse.Failed -> {
                        if (response.failReason == "insufficient_tokens") {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showWarning(
                                    activity,
                                    "Yetersiz jeton! Gerekli: ${response.requiredTokens}, Mevcut: ${response.currentBalance}"
                                )
                            }
                        } else {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, response.error)
                            }
                        }
                    }
                    is SendMessageResponse.Error -> {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, response.message)
                        }
                    }
                }
            }
        }
    }
    
    // Resim mesajı gönderme fonksiyonu
    fun sendImageMessage(imageUrl: String) {
        if (imageUrl.isBlank() || isSending) return
        
        isSending = true
        val tempId = java.util.UUID.randomUUID().toString()
        socketManager.sendMessage(
            receiverId = otherUserId,
            content = "📷 Resim", // API için minimum content gerekiyor
            type = "image",
            mediaUrl = imageUrl,
            tempId = tempId
        ) { response ->
            coroutineScope.launch {
                isSending = false
                isUploadingImage = false
                
                when (response) {
                    is SendMessageResponse.Success -> {
                        // Conversation ID'yi sakla
                        if (currentConversationId == null) {
                            currentConversationId = response.conversationId
                        }
                        // Artık mesaj var, template'leri gizle
                        hasMessages = true
                        // Chat'i refresh et
                        refreshTrigger++
                        
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showSuccess(activity, "Resim gönderildi")
                        }
                    }
                    is SendMessageResponse.Failed -> {
                        if (response.failReason == "insufficient_tokens") {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showWarning(
                                    activity,
                                    "Yetersiz jeton! Gerekli: ${response.requiredTokens}, Mevcut: ${response.currentBalance}"
                                )
                            }
                        } else {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, response.error)
                            }
                        }
                    }
                    is SendMessageResponse.Error -> {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, response.message)
                        }
                    }
                }
            }
        }
    }
    
    // Eğer conversation varsa, mesaj sayısını kontrol et
    LaunchedEffect(currentConversationId) {
        val convId = currentConversationId // Smart cast için yerel değişkene ata
        if (convId != null) {
            isCheckingMessages = true
            coroutineScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        ApiClient.getInstance(context).messageService.getMessages(
                            conversationId = convId,
                            page = 1,
                            limit = 1
                        )
                    }
                    hasMessages = response.messages.isNotEmpty()
                } catch (e: Exception) {
                    hasMessages = true // Hata durumunda template gösterme
                } finally {
                    isCheckingMessages = false
                }
            }
        } else {
            // Yeni conversation ise template'leri göster
            hasMessages = false
            isCheckingMessages = false
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NewMessageToolbar(
                onBack = onBack,
                profileName = otherUserName,
                profilePhotoUrl = otherUserPhotoUrl,
                showDropdown = showDropdownMenu,
                onMoreClick = { showDropdownMenu = true },
                onDismissDropdown = { showDropdownMenu = false },
                isBlocked = isBlocked,
                onBlockClick = {
                    showDropdownMenu = false
                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                if (isBlocked) {
                                    ApiClient.getInstance(context).userService.unblockUser(
                                        BlockRequest(targetUserId = otherUserId)
                                    )
                                } else {
                                    ApiClient.getInstance(context).userService.blockUser(
                                        BlockRequest(targetUserId = otherUserId)
                                    )
                                }
                            }
                            
                            if (response.success && response.data != null) {
                                isBlocked = response.data.isBlocked
                                val message = if (isBlocked) "Kullanıcı engellendi" else "Kullanıcının engeli kaldırıldı"
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showSuccess(activity, message)
                                }
                            } else {
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showError(activity, response.message ?: "İşlem başarısız")
                                }
                            }
                        } catch (e: Exception) {
                            val errorMessage = ApiException.getErrorMessage(e)
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, errorMessage)
                            }
                        }
                    }
                },
                onReportClick = {
                    showDropdownMenu = false
                    ReportActivity.start(context, otherUserId, otherUserName)
                },
                onGiftClick = { showGiftPicker = true }
            )
        },
        bottomBar = {
            Column {
                // Template mesajlar - conversation yoksa VEYA (mesaj listesi boş VE kontrol bitti) göster
                val showTemplates = (currentConversationId == null || (!hasMessages && !isCheckingMessages)) && message.isEmpty()
                if (showTemplates) {
                    TemplateMessagesView(
                        profileName = otherUserName,
                        onTemplateClick = { templateMessage ->
                            // Template'e tıklanınca direkt gönder
                            sendMessage(templateMessage)
                        }
                    )
                }
                
                MessageInputView(
                    value = message,
                    onValueChange = { message = it },
                    enabled = !isSending && !isUploadingImage,
                    onLeftIconClick = {
                        // Attach butonu - Resim seçici aç
                        showImagePicker = true
                    },
                    onRightIconClick = {
                        sendMessage(message)
                    },
                    replyMessage = replyMessage,
                    onReplyCancel = { replyMessage = null },
                    modifier = Modifier.imePadding()
                )
            }
        }
    ) { innerPadding ->
        // Chat içeriği
        ChatContent(
            conversationId = currentConversationId,
            otherUserId = otherUserId,
            otherUserName = otherUserName,
            refreshTrigger = refreshTrigger,
            onResendMessage = { failedMessage ->
                // Başarısız mesajı tekrar gönder (mesaj ID'sini resendMessageId olarak gönder)
                sendMessage(failedMessage.content, failedMessage.id ?: failedMessage.tempId)
            },
            onMessagesEmpty = {
                // Tüm mesajlar silindiyse template mesajları göster
                hasMessages = false
            },
            onReplyMessage = { messageToReply ->
                // Mesajı reply olarak ayarla
                replyMessage = ReplyMessage(
                    id = messageToReply.id ?: messageToReply.tempId ?: "",
                    senderId = messageToReply.senderId,
                    senderName = messageToReply.senderName,
                    content = messageToReply.content,
                    type = messageToReply.type,
                    mediaUrl = messageToReply.mediaUrl
                )
            },
            listState = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
    
    // Resim seçici bottom sheet
    if (!isUploadingImage) {
        ImagePickerBottomSheet(
            isVisible = showImagePicker,
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                showImagePicker = false
                if (uri != null) {
                    isUploadingImage = true

                    coroutineScope.launch {
                        try {
                            val fileUploadService = FileUploadService(context)
                            val result = fileUploadService.uploadImage(uri, folder = "messages")
                            
                            result.onSuccess { imageUrl ->
                                // Upload başarılı, resimli mesaj gönder
                                sendImageMessage(imageUrl)
                            }.onFailure { error ->
                                isUploadingImage = false
                                isSending = false
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showError(
                                        activity, 
                                        "Resim yüklenemedi: ${error.message}"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            isUploadingImage = false
                            isSending = false
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, "Resim yüklenemedi")
                            }
                        }
                    }
                }
            },
            onError = { error ->
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, error)
                }
            }
        )
    }
    
    // Hediye seçim bottom sheet
    GiftPickerBottomSheet(
        isVisible = showGiftPicker,
        onDismiss = { showGiftPicker = false },
        onGiftSelected = { gift ->
            // Hediye seçildi, hediye mesajı gönder
            sendGiftMessage(gift)
        },
        onError = { error ->
            (context as? Activity)?.let { activity ->
                ToastHelper.showError(activity, error)
            }
        }
    )
}

@Composable
fun NewMessageToolbar(
    onBack: () -> Unit,
    profileName: String,
    profilePhotoUrl: String?,
    showDropdown: Boolean,
    onMoreClick: () -> Unit,
    onDismissDropdown: () -> Unit,
    isBlocked: Boolean,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit,
    onGiftClick: () -> Unit
) {
    val shape: Shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.statusBarsPadding())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .shadow(elevation = 8.dp, shape = shape, clip = false)
                .clip(shape)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geri butonu
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Geri",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
                tint = Color.Black
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Profil fotoğrafı
            if (!profilePhotoUrl.isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = profilePhotoUrl,
                    contentDescription = "Profil Fotoğrafı",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Profil ismi
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            // Hediye gönder butonu - Kart içinde Lottie animasyonu
            Card(
                onClick = { 
                    onGiftClick()
                },
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val composition = rememberLottieComposition(
                        LottieCompositionSpec.RawRes(com.matcher.matcher.R.raw.gift)
                    ).value
                    
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            // More menu
            Box {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Daha fazla",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMoreClick() },
                    tint = Color.Black
                )
                
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = onDismissDropdown,
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBlocked) "Engeli Kaldır" else "Engelle",
                                color = if (isBlocked) colorResource(id = R.color.primaryColor) else Color(0xFFE53935)
                            )
                        },
                        onClick = onBlockClick
                    )
                    
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Şikayet Et",
                                color = Color(0xFFFF6F00)
                            )
                        },
                        onClick = onReportClick
                    )
                }
            }
        }
    }
}
