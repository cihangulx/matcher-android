package com.flort.evlilik.modules.main.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flort.evlilik.models.profile.Profile
import com.flort.evlilik.models.profile.GalleryImage
import com.flort.evlilik.models.user.BlockedUser
import com.flort.evlilik.models.user.request.BlockRequest
import com.flort.evlilik.modules.main.components.BlockedGridItemView
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.utils.helpers.ToastHelper
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BlockedScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val blockedProfiles = remember { mutableStateListOf<Profile>() }
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    
    // API'den engellenen kullanıcıları çek
    LaunchedEffect(Unit) {
        try {
            isLoading.value = true
            errorMessage.value = null
            
            val response = withContext(Dispatchers.IO) {
                ApiClient.getInstance(context).userService.getBlockedUsers()
            }
            
            if (response.success && response.data != null) {
                // BlockedUser'ı Profile'a dönüştür
                val profiles = response.data.map { blockedUser ->
                    val profile = Profile().apply {
                        userId = blockedUser._id
                        name = blockedUser.name
                        age = blockedUser.age
                        like = blockedUser.like
                        gallery = ArrayList(blockedUser.gallery?.map { galleryItem ->
                            GalleryImage().apply {
                                index = galleryItem.index
                                url = galleryItem.url
                                isMain = galleryItem.isMain
                            } ?: GalleryImage()
                        } ?: emptyList())
                    }
                    profile
                }
                blockedProfiles.clear()
                blockedProfiles.addAll(profiles)
            } else {
                errorMessage.value = response.message ?: "Engellenen kullanıcılar yüklenemedi"
            }
        } catch (e: Exception) {
            errorMessage.value = "Bağlantı hatası: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }
    
    // Engeli kaldır fonksiyonu
    val onUnblock = { profile: Profile ->
        coroutineScope.launch {
            try {
                val userId = profile.userId ?: return@launch
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getInstance(context).userService.unblockUser(
                        BlockRequest(targetUserId = userId)
                    )
                }
                
                val activity = context as? Activity
                if (response.success) {
                    blockedProfiles.remove(profile)
                    activity?.let {
                        ToastHelper.showSuccess(it, "Engel kaldırıldı")
                    }
                } else {
                    activity?.let {
                        ToastHelper.showError(it, response.message ?: "Engel kaldırılamadı")
                    }
                }
            } catch (e: Exception) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, "Bağlantı hatası: ${e.message}")
                }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Yükleniyor...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
            errorMessage.value != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage.value ?: "Bir hata oluştu",
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            blockedProfiles.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Engellenen kullanıcı bulunmuyor",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(blockedProfiles) { profile ->
                        // Bugünün tarihini formatla (gerçek tarih bilgisi API'den gelmiyor)
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        val date = dateFormat.format(Date())
                        
                        BlockedGridItemView(
                            profile = profile,
                            date = date,
                            onUnblock = { onUnblock(profile) }
                        )
                    }
                }
            }
        }
    }
}

