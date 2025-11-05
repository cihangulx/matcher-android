package com.flort.evlilik.modules.main.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flort.evlilik.utils.events.ProfileEvent
import com.flort.evlilik.utils.events.ProfileEventManager
import com.flort.evlilik.models.filter.ProfileFilter
import com.flort.evlilik.models.profile.Profile
import com.flort.evlilik.models.user.User
import com.flort.evlilik.models.user.copy
import com.flort.evlilik.modules.main.components.HomeProfileItemView
import com.flort.evlilik.modules.main.profile.ProfileActivity
import com.flort.evlilik.modules.main.message.NewMessageActivity
import com.flort.evlilik.components.ProfileSkeletonView
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.models.profile.like.LikeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    filter: ProfileFilter = ProfileFilter.DEFAULT
) {
    val context = LocalContext.current
    val allProfiles = remember { mutableStateOf<List<User>>(emptyList()) }
    val filteredProfiles = remember { mutableStateOf<List<User>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    val isFiltering = remember { mutableStateOf(false) }
    val emptyViewVisible = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val refreshTrigger = remember { mutableStateOf(0) }
    
    // Profil event'lerini dinle
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            ProfileEventManager.events.collect { event ->
                when (event) {
                    is ProfileEvent.ProfileLikeChanged -> {
                        // ProfileActivity'den gelen beğeni değişikliklerini uygula
                        allProfiles.value = allProfiles.value.map { user ->
                            if (user._id == event.userId) {
                                // User objesini kopyala ve sadece beğeni bilgilerini güncelle
                                user.copy(
                                    isLiked = event.isLiked,
                                    like = event.likeCount ?: user.like
                                )
                            } else {
                                user
                            }
                        }
                    }
                    is ProfileEvent.ProfileBlockChanged -> {
                        // Engellenen kullanıcı listeden kaldırılmalı
                        if (event.isBlocked) {
                            allProfiles.value = allProfiles.value.filter { it._id != event.userId }
                        } else {
                            // Engel kaldırıldığında tüm listeyi yenile
                            refreshTrigger.value++
                        }
                    }
                    is ProfileEvent.ProfileReported -> {
                        // Rapor edilen kullanıcı için özel işlem yapılabilir
                        // Şimdilik refresh yapalım
                        refreshTrigger.value++
                    }
                    is ProfileEvent.RefreshRequired -> {
                        // Genel refresh
                        refreshTrigger.value++
                    }
                }
            }
        }
        
        onDispose {
            job.cancel()
            // Socket bağlantısını KESMİYORUZ - MainActivity'de kalmalı
        }
    }

    // API'den tüm profilleri çek
    LaunchedEffect(refreshTrigger.value) {
        try {
            isLoading.value = true
            errorMessage.value = null
            
            val response = withContext(Dispatchers.IO) {
                ApiClient.getInstance(context).profileService.getHomeProfiles()
            }
            
            if (response.success && response.data != null) {
                allProfiles.value = response.data
            } else {
                errorMessage.value = response.message ?: "Profiller yüklenemedi"
            }
        } catch (e: Exception) {
            errorMessage.value = "Bağlantı hatası: ${e.message}"
        } finally {
            isLoading.value = false
        }
    }
    
    // Filtrelemeyi client-side yap
    LaunchedEffect(allProfiles.value, filter) {
        if (allProfiles.value.isNotEmpty()) {
            // Sadece filtre değiştiğinde filtreleme state'ini aktif et
            val hasActiveFilter = filter.minAge != null || filter.maxAge != null || filter.gender != null
            if (hasActiveFilter) {
                isFiltering.value = true
                emptyViewVisible.value = false // Filtreleme başladığında empty view'ı gizle
            }
            
            filteredProfiles.value = allProfiles.value.filter { user ->
                var matches = true
                
                // Yaş filtresi - smart cast sorunu için local variable kullan
                val userAge = user.age
                
                // Minimum yaş kontrolü
                if (filter.minAge != null && userAge != null) {
                    matches = matches && userAge >= filter.minAge
                }
                
                // Maximum yaş kontrolü - null ise sınırsız (50+ gibi)
                if (filter.maxAge != null && userAge != null) {
                    matches = matches && userAge <= filter.maxAge
                }
                // maxAge null ise kontrol yapma (50+ tüm yaşları içerir)
                
                // Cinsiyet filtresi (ileride eklenebilir)
                val userGender = user.gender
                if (filter.gender != null && userGender != null) {
                    matches = matches && userGender == filter.gender
                }
                
                matches
            }
            
            if (hasActiveFilter) {
                isFiltering.value = false
                // Filtreleme tamamlandığında, eğer sonuç boşsa empty view'ı göster
                if (filteredProfiles.value.isEmpty()) {
                    emptyViewVisible.value = true
                }
            } else {
                // Filtre yoksa empty view'ı gizle
                emptyViewVisible.value = false
            }
        }
    }

    // Loading state - Skeleton loading göster
    // API yükleniyor VEYA filtreleme yapılıyor
    if (isLoading.value || isFiltering.value) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(5) { // 5 adet skeleton göster - daha gerçekçi
                ProfileSkeletonView()
            }
        }
        return
    }

    // Error state - Daha kullanıcı dostu hata mesajı
    if (errorMessage.value != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Profiller yüklenirken bir sorun oluştu",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lütfen tekrar deneyin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Empty state - sadece emptyViewVisible true olduğunda göster
    if (emptyViewVisible.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "😔",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Filtrelere uygun profil bulunamadı",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Farklı filtreler deneyin veya filtreleri temizleyin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Profil listesi
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(
            items = filteredProfiles.value,
            key = { user -> user._id ?: user.hashCode() }
        ) { user ->
            val profile = Profile().apply {
                userId = user._id
                name = user.name
                age = user.age
                statusDesc = user.desc ?: ""
                
                // Galeriyi ayarla
                gallery = user.gallery ?: ArrayList()
                
                // Beğeni bilgilerini aktar
                like = user.like ?: 0
                isLiked = user.isLiked ?: false
            }
            
    // Örnek profil verileri - artık kullanılmıyor
    /*val sampleProfiles = remember {
        listOf(
            Profile().apply {
                name = "Ayşe"
                age = 24
                statusDesc = "Yeni arkadaşlar arıyorum"
                gallery = arrayListOf(
                    GalleryImage().apply { url = "https://example.com/profile1.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile3.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile4.jpg" }
                )
            },
            Profile().apply {
                name = "Seda"
                age = 26
                statusDesc = "Spor yapmayı seviyorum"
                gallery = arrayListOf(
                    GalleryImage().apply { url = "https://example.com/profile5.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile1.jpg" }
                )
            },
            Profile().apply {
                name = "Zeynep"
                age = 22
                statusDesc = "Müzik dinlemeyi seviyorum"
                gallery = arrayListOf(
                    GalleryImage().apply { localImage = com.flort.evlilik.R.mipmap.temp_profile_3 },
                    GalleryImage().apply { localImage = com.flort.evlilik.R.mipmap.temp_profile_5 }
                )
            },
            Profile().apply {
                name = "Canan"
                age = 28
                statusDesc = "Yolculuk yapmayı seviyorum"
                gallery = arrayListOf(
                    GalleryImage().apply { url = "https://example.com/profile4.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile1.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile2.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile3.jpg" }
                )
            },
            Profile().apply {
                name = "Elif"
                age = 25
                statusDesc = "Sanat ve fotoğrafçılık"
                gallery = arrayListOf(
                    GalleryImage().apply { url = "https://example.com/profile5.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile3.jpg" },
                    GalleryImage().apply { url = "https://example.com/profile5.jpg" }
                )
            }
        )
    }*/

            HomeProfileItemView(
                profile = profile,
                onLikeClick = { clickedProfile ->
                    // Beğeni işlemi (like/unlike)
                    coroutineScope.launch {
                        try {
                            val userId = user._id ?: return@launch
                            val isCurrentlyLiked = user.isLiked ?: false
                            
                            val response = withContext(Dispatchers.IO) {
                                if (isCurrentlyLiked) {
                                    // Unlike işlemi
                                    ApiClient.getInstance(context).profileService.unlikeProfile(
                                        LikeRequest(targetUserId = userId)
                                    )
                                } else {
                                    // Like işlemi
                                    ApiClient.getInstance(context).profileService.likeProfile(
                                        LikeRequest(targetUserId = userId)
                                    )
                                }
                            }
                            
                            if (response.success) {
                                // Listeyi güncelle - User objesini kopyala
                                allProfiles.value = allProfiles.value.map { u ->
                                    if (u._id == userId) {
                                        u.copy(
                                            like = if (isCurrentlyLiked) {
                                                maxOf((u.like ?: 0) - 1, 0)
                                            } else {
                                                (u.like ?: 0) + 1
                                            },
                                            isLiked = !isCurrentlyLiked
                                        )
                                    } else {
                                        u
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Hata durumunda sessiz kal
                        }
                    }
                },
                onMessageClick = {
                    // Mesajlaşma başlat (mevcut conversation varsa onu açar)
                    user._id?.let { userId ->
                        NewMessageActivity.startMessaging(
                            context = context,
                            otherUserId = userId,
                            otherUserName = user.name ?: "Kullanıcı",
                            otherUserPhotoUrl = user.gallery?.find { it.isMain == true }?.url 
                                ?: user.gallery?.firstOrNull()?.url
                        )
                    }
                },
                onProfileClick = {
                    // Güncel profile objesini kullan
                    ProfileActivity.start(context, profile)
                }
            )
        }
    }
}