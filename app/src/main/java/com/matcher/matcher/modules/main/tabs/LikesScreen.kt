package com.matcher.matcher.modules.main.tabs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.matcher.matcher.R
import com.matcher.matcher.models.profile.Profile
import com.matcher.matcher.modules.main.components.LikesGridItemView
import com.matcher.matcher.modules.main.profile.ProfileActivity
import com.matcher.matcher.modules.wallet.VipActivity
import com.matcher.matcher.components.LikesGridSkeletonList
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.models.profile.like.LikedUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LikesScreen(searchQuery: String = "") {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val isVisible = remember { mutableStateOf(true) }
    val isPremium = remember { mutableStateOf(false) }
    val totalLikes = remember { mutableStateOf(0) }
    val likedUsers = remember { mutableStateOf<List<LikedUser>>(emptyList()) }
    val premiumMessage = remember { mutableStateOf<String?>(null) }
    val refreshTrigger = remember { mutableStateOf(0) }
    
    // Filtrelenmiş beğeniler
    val filteredLikedUsers = remember(likedUsers.value, searchQuery) {
        if (searchQuery.isBlank()) {
            likedUsers.value
        } else {
            likedUsers.value.filter { user: LikedUser ->
                user.name?.contains(searchQuery, ignoreCase = true) == true ||
                user.desc?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // VIP satın alma için Activity Result launcher
    val vipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // VIP satın alma sonrası sayfayı yenile
        refreshTrigger.value++
    }
    
    // Veri yükleme fonksiyonu
    fun loadLikesData() {
        coroutineScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getInstance(context).profileService.getMyLikes()
                }
                
                if (response.success && response.data != null) {
                    isVisible.value = response.data.isVisible
                    isPremium.value = response.data.isPremium
                    totalLikes.value = response.data.totalLikes
                    likedUsers.value = response.data.users
                    premiumMessage.value = response.data.message
                } else {
                    errorMessage.value = response.message ?: "Beğeniler yüklenemedi"
                }
            } catch (e: Exception) {
                errorMessage.value = "Bağlantı hatası: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    // API'den beğenileri çek - ilk yükleme ve refresh trigger değiştiğinde
    LaunchedEffect(refreshTrigger.value) {
        loadLikesData()
    }
    
    // Loading state - Skeleton loading göster
    if (isLoading.value) {
        LikesGridSkeletonList(itemCount = 6) // 2x3 grid için 6 skeleton
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Beğeniler yüklenirken bir sorun oluştu",
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
    
    // Boş liste durumu - Daha kullanıcı dostu mesaj
    if (filteredLikedUsers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_heart_outline),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (searchQuery.isBlank()) "Henüz kimse seni beğenmemiş" else "Arama sonucu bulunamadı",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                if (searchQuery.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Profilini güncelleyerek daha çok beğeni alabilirsin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }
    
    // Beğenileri grid olarak göster
    Box(modifier = Modifier.fillMaxSize()) {
        // Grid - her durumda göster
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredLikedUsers,
                key = { user -> user._id }
            ) { user ->
                val profile = Profile().apply {
                    userId = user._id
                    name = user.name
                    age = user.age
                    statusDesc = user.desc ?: ""
                    gallery = user.gallery ?: ArrayList()
                    like = user.like
                    isLiked = user.isLikedBack
                }
                
                LikesGridItemView(
                    profile = profile,
                    selected = user.isLikedBack == true,
                    isBlurred = !isPremium.value || !isVisible.value,
                    onClick = { clickedProfile ->
                        // Premium değilse tıklanamaz
                        if (isPremium.value && isVisible.value) {
                            ProfileActivity.start(context, clickedProfile)
                        }
                    }
                )
            }
        }
        
        // Premium olmayan kullanıcılar için overlay
        if (!isVisible.value || !isPremium.value) {
            // Blur efekti için arka plan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )
            
            // Premium card overlay
            androidx.compose.material3.Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(24.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Premium Lottie animasyonu
                    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.premium)).value
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(100.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Başlık
                    Text(
                        text = "Premium Özellik",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Toplam beğeni sayısı
                    Text(
                        text = "Seni ${totalLikes.value} kişi beğendi!",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFFFA500),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Açıklama
                    Text(
                        text = premiumMessage.value ?: "Bu özelliği kullanmak için premium üyelik gereklidir",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Premium satın al butonu
                    Button(
                        onClick = {
                            val intent = android.content.Intent(context, com.matcher.matcher.modules.wallet.VipActivity::class.java)
                            vipLauncher.launch(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA500)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Premium animasyon butonda
                        val buttonComposition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.premium)).value
                        LottieAnimation(
                            composition = buttonComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Premium Satın Al",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}