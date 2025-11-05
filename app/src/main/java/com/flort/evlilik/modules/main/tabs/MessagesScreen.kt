package com.flort.evlilik.modules.main.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.flort.evlilik.models.user.User
import com.flort.evlilik.modules.main.components.ConversationItemView
import com.flort.evlilik.modules.main.message.NewMessageActivity
import com.flort.evlilik.components.ConversationSkeletonView
import com.flort.evlilik.models.message.core.Conversation
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.network.socket.SocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MessagesScreen(searchQuery: String = "") {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val socketManager = SocketManager.getInstance()
    
    // State'ler
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Filtrelenmiş konuşmalar
    val filteredConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter { conversation ->
                val currentUserId = User.current?._id ?: ""
                val otherUser = conversation.getOtherUser(currentUserId)
                otherUser?.name?.contains(searchQuery, ignoreCase = true) == true ||
                conversation.lastMessage?.content?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // Konuşmaları yükle fonksiyonu
    fun loadConversations() {
        coroutineScope.launch {
            try {
                isLoading = true

                val response = withContext(Dispatchers.IO) {
                    ApiClient.getInstance(context).messageService.getConversations()
                }
                if (response.success) {
                    // Null-safety kontrolü
                    val validConversations = response.conversations.filter { conversation ->
                        val isValid = conversation.otherUser != null || !conversation.participants.isNullOrEmpty()
                        if (!isValid) {
                        }
                        isValid
                    }
                    
                    conversations = validConversations
                } else {
                    // API başarısız - sessizce boş liste göster
                }
            } catch (e: Exception) {
                // Hata durumunda sessizce boş liste göster - kullanıcı rahatsız edilmez
            } finally {
                isLoading = false
            }
        }
    }
    
    // İlk yükleme
    LaunchedEffect(refreshTrigger) {
        loadConversations()
    }
    
    // Ekran her görünür olduğunda (Resume) listeyi yenile
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Socket'ten yeni mesajları ve okunma durumlarını dinle
    LaunchedEffect(Unit) {
        // Yeni mesaj geldiğinde listeyi yenile
        launch {
            socketManager.incomingMessages.collect { event ->
                if (event != null) {
                    refreshTrigger++
                }
            }
        }
    }
    
    // Loading durumu - Skeleton loading göster
    if (isLoading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            items(5) { // 5 adet skeleton göster - daha gerçekçi
                ConversationSkeletonView()
            }
        }
        return
    }
    
    // Boş durum - Daha kullanıcı dostu mesaj
    if (filteredConversations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "Henüz mesaj yok" else "Arama sonucu bulunamadı",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yeni arkadaşlar bul ve sohbet etmeye başla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }
    
    // Konuşma listesi
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        items(
            items = filteredConversations,
            key = { conversation -> conversation.id }
        ) { conversation ->
            val currentUserId = User.current?._id ?: ""
            val otherUser = conversation.getOtherUser(currentUserId)
            
            // Sadece geçerli konuşmaları göster
            if (otherUser != null && otherUser.id.isNotEmpty()) {
                ConversationItemView(
                    conversation = conversation,
                    currentUserId = currentUserId,
                    onClick = {
                        // Mevcut konuşmayı aç
                        NewMessageActivity.openConversation(
                            context = context,
                            conversationId = conversation.id,
                            otherUserId = otherUser.id,
                            otherUserName = otherUser.name ?: "Kullanıcı",
                            otherUserPhotoUrl = otherUser.getPhotoUrl()
                        )
                    }
                )
            } else {
            }
        }
    }
}