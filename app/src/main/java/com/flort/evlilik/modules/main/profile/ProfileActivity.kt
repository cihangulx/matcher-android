package com.flort.evlilik.modules.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.app.Activity
import androidx.activity.ComponentActivity
import com.flort.evlilik.utils.helpers.ToastHelper
import com.flort.evlilik.network.model.ApiException
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.flort.evlilik.utils.events.ProfileEventManager
import com.flort.evlilik.models.profile.GalleryImage
import com.flort.evlilik.models.profile.Profile
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.models.profile.like.LikeRequest
import com.flort.evlilik.models.user.request.BlockRequest
import com.flort.evlilik.modules.main.message.NewMessageActivity
import kotlinx.coroutines.Dispatchers
import com.flort.evlilik.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : ComponentActivity() {
    companion object {
        var currentProfile: Profile? = null
        
        fun start(context: Context, profile: Profile) {
            currentProfile = profile
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val profile = currentProfile ?: getSampleProfile()
        
        setContent {
            ProfileScreen(
                profile = profile,
                onBack = { finish() }
            )
        }
    }
    
    private fun getSampleProfile(): Profile {
        return Profile().apply {
            userId = "sample_user_id"
            name = "Ayşe"
            age = 25
            statusDesc = "Hayatın tadını çıkarıyorum"
            like = 42
            isLiked = false
            gallery = arrayListOf(
                GalleryImage().apply { url = "https://example.com/profile1.jpg" },
                GalleryImage().apply { url = "https://example.com/profile2.jpg" },
                GalleryImage().apply { url = "https://example.com/profile3.jpg" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: Profile,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isLiked by remember(profile.isLiked) { mutableStateOf(profile.isLiked ?: false) }
    var likeCount by remember(profile.like) { mutableStateOf(profile.like ?: 0) }
    var isBlocked by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { profile.gallery.size })
    
    // Profile değiştiğinde state'i güncelle
    LaunchedEffect(profile.isLiked, profile.like) {
        isLiked = profile.isLiked ?: false
        likeCount = profile.like ?: 0
    }
    
    // Engel durumunu kontrol et
    LaunchedEffect(profile.userId) {
        profile.userId?.let { userId ->
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getInstance(context).userService.checkBlockStatus(userId)
                }
                if (response.success && response.data != null) {
                    isBlocked = response.data.isBlocked
                }
            } catch (e: Exception) {
                // Hata durumunda varsayılan değer false kalacak
            }
        }
    }
    
    // Beğeni işlemi
    fun handleLikeClick() {
        // userId kontrolü
        val userId = profile.userId
        if (userId == null) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Kullanıcı ID bulunamadı")
            }
            return
        }
        
        coroutineScope.launch {
            try {
                val currentLikeStatus = isLiked
                val currentLikeCount = likeCount
                
                // Local state'i hemen güncelle (optimistic update)
                isLiked = !currentLikeStatus
                likeCount = if (!currentLikeStatus) {
                    currentLikeCount + 1
                } else {
                    maxOf(currentLikeCount - 1, 0)
                }
                
                // API çağrısı
                val response = withContext(Dispatchers.IO) {
                    if (currentLikeStatus) {
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
                
                if (!response.success) {
                    // Hata durumunda geri al
                    isLiked = currentLikeStatus
                    likeCount = currentLikeCount
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, response.message ?: "İşlem başarısız")
                    }
                } else {
                    // Başarılı olduğunda event gönder - güncellenmiş like count ile
                    ProfileEventManager.notifyProfileLikeChanged(userId, !currentLikeStatus, likeCount)
                }
            } catch (e: Exception) {
                // Hata durumunda state'i geri al
                isLiked = !isLiked
                likeCount = if (isLiked) {
                    maxOf(likeCount - 1, 0)
                } else {
                    likeCount + 1
                }
                val errorMessage = ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMessage)
                }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Arka plan resmi
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
        // Custom Toolbar (SettingsActivity gibi)
        ProfileToolbar(
            onBack = onBack,
            title = "${profile.name ?: "Profil"}, ${profile.age ?: "?"}",
            onMoreClick = { showDropdownMenu = true },
            showDropdown = showDropdownMenu,
            onDismissDropdown = { showDropdownMenu = false },
            isBlocked = isBlocked,
            onBlockClick = {
                showDropdownMenu = false
                val userId = profile.userId
                if (userId != null) {
                    coroutineScope.launch {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                if (isBlocked) {
                                    ApiClient.getInstance(context).userService.unblockUser(
                                        BlockRequest(targetUserId = userId)
                                    )
                                } else {
                                    ApiClient.getInstance(context).userService.blockUser(
                                        BlockRequest(targetUserId = userId)
                                    )
                                }
                            }
                            
                            if (response.success && response.data != null) {
                                isBlocked = response.data.isBlocked
                                val message = if (isBlocked) "Kullanıcı engellendi" else "Kullanıcının engeli kaldırıldı"
                                (context as? Activity)?.let { activity ->
                                    ToastHelper.showSuccess(activity, message)
                                }
                                
                                // Event gönder
                                ProfileEventManager.notifyProfileBlockChanged(userId, isBlocked)
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
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showWarning(activity, "Kullanıcı ID bulunamadı")
                    }
                }
            },
            onReportClick = {
                showDropdownMenu = false
                val userId = profile.userId
                val userName = profile.name
                if (userId != null && userName != null) {
                    ReportActivity.start(context, userId, userName)
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showWarning(activity, "Kullanıcı bilgileri bulunamadı")
                    }
                }
            }
        )
        
        // İçerik
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Galeri Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Box {
                    // Galeri
                    if (profile.gallery.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val galleryItem = profile.gallery[page]
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            ) {
                                when {
                                    galleryItem.url != null -> {
                                        // URL'den resim yükle
                                        SubcomposeAsyncImage(
                                            model = galleryItem.url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFFD3D3D3)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(48.dp),
                                                        color = colorResource(id = R.color.primaryColor)
                                                    )
                                                }
                                            },
                                            error = {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color(0xFFD3D3D3)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_logo),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(48.dp),
                                                        tint = Color(0xFF808080)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    !galleryItem.url.isNullOrEmpty() -> {
                                        // URL'den resim yükle
                                        AsyncImage(
                                            model = galleryItem.url,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    else -> {
                                        // Varsayılan resim
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFD3D3D3)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_logo),
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color(0xFF808080)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Dots Indicator
                        if (profile.gallery.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x4D000000),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(profile.gallery.size) { index ->
                                        val isSelected = index == pagerState.currentPage
                                        val dotSizeDp = if (isSelected) 8.dp else 6.dp
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(dotSizeDp)
                                                .background(
                                                    color = if (isSelected) Color(0xFFFFFFFF) else Color(0x80FFFFFF),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                            }
                        }
                        
                        // Sağ alt köşede beğeni butonu
                        IconButton(
                            onClick = { handleLikeClick() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x80000000),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isLiked) R.drawable.likes_selected else R.drawable.likes
                                        ),
                                        contentDescription = if (isLiked) "Beğenildi" else "Beğen",
                                        modifier = Modifier.size(24.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    } else {
                        // Galeri boşsa varsayılan görünüm
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(Color(0xFFD3D3D3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF808080)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Hakkımda Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Hakkımda",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF000000)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile.statusDesc ?: "Henüz bir açıklama eklenmemiş.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF808080),
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mesaj Gönder Butonu
            Button(
                onClick = {
                    // Mesajlaşma ekranına yönlendir (mevcut conversation varsa onu açar)
                    profile.userId?.let { userId ->
                        NewMessageActivity.startMessaging(
                            context = context,
                            otherUserId = userId,
                            otherUserName = profile.name ?: "Kullanıcı",
                            otherUserPhotoUrl = profile.getPp()
                        )
                    } ?: run {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showWarning(activity, "Kullanıcı bilgisi bulunamadı")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.primaryColor),
                    contentColor = Color(0xFFFFFFFF)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.home_send_message),
                    contentDescription = "Mesaj Gönder",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mesaj Gönder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}

@Composable
private fun ProfileToolbar(
    onBack: () -> Unit,
    title: String,
    onMoreClick: () -> Unit,
    showDropdown: Boolean,
    onDismissDropdown: () -> Unit,
    isBlocked: Boolean,
    onBlockClick: () -> Unit,
    onReportClick: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Geri butonu
            Icon(
                painter = painterResource(id = R.drawable.left_arrow),
                contentDescription = "Geri",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBack() },
                tint = Color.Black
            )
            
            // Başlık
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            // More menu
            Box {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vert),
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
