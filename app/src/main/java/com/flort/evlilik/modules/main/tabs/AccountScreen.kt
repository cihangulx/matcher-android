package com.flort.evlilik.modules.main.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.colorResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flort.evlilik.modules.main.components.AccountListItemView
import com.flort.evlilik.modules.account.SettingsActivity
import com.flort.evlilik.models.user.User
import com.airbnb.lottie.compose.*
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import com.flort.evlilik.network.Routes
import kotlinx.coroutines.launch
import com.flort.evlilik.modules.terms.TermsActivity
import com.flort.evlilik.modules.main.components.Section
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.utils.PreferencesManager
import com.flort.evlilik.utils.helpers.ToastHelper
import android.app.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.flort.evlilik.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val showBlocked = remember { mutableStateOf(false) }
    val showContact = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // API ve Preferences Manager
    val apiClient = ApiClient.getInstance(context)
    val preferencesManager = PreferencesManager.getInstance(context)
    
    // User state'i için
    var currentUser by remember { mutableStateOf(User.current) }
    
    // Hakkımda düzenleme state'i
    var isEditingAbout by remember { mutableStateOf(false) }
    var aboutText by remember { mutableStateOf(currentUser?.desc ?: "") }
    
    // Hakkımda güncelleme fonksiyonu
    fun updateAboutText() {
        if (isLoading.value) return
        
        coroutineScope.launch {
            isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    val userService = apiClient.userService
                    val request = com.flort.evlilik.models.user.request.UpdateInfoRequest(
                        name = currentUser?.name ?: "",
                        age = currentUser?.age,
                        gender = currentUser?.gender,
                        desc = aboutText
                    )
                    
                    val response = userService.updateInfo(request)
                    
                    withContext(Dispatchers.Main) {
                        if (response.success) {
                            // User'ı güncelle
                            currentUser?.desc = aboutText
                            isEditingAbout = false
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showSuccess(activity, "Hakkımda bilgisi güncellendi")
                            }
                        } else {
                            (context as? Activity)?.let { activity ->
                                ToastHelper.showError(activity, response.message ?: "Güncelleme hatası")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, "Bağlantı hatası: ${e.message}")
                }
            } finally {
                isLoading.value = false
            }
        }
    }
    
    // Ekran görünür olduğunda User.current'ı kontrol et ve güncelle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // User.current güncellenmiş olabilir
                currentUser = User.current
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Logout fonksiyonu
    fun performLogout() {
        if (isLoading.value) return
        
        coroutineScope.launch {
            isLoading.value = true
            try {
                // API'ye logout isteği gönder (başarısız olsa bile devam et)
                withContext(Dispatchers.IO) {
                    try {
                        apiClient.authService.logout()
                    } catch (e: Exception) {
                        // API hatası görmezden gelinir
                    }
                }
            } catch (e: Exception) {
                // API hatası görmezden gelinir
            } finally {
                // Her durumda local'den çıkış yap
                try {
                    // Token'ı temizle
                    withContext(Dispatchers.IO) {
                        preferencesManager.clearAuthToken()
                    }
                    
                    // User'ı temizle
                    User.current = null
                    
                    // Başarı mesajı göster
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Başarıyla çıkış yapıldı")
                    }
                    
                    // MainActivity'ye logout sinyali gönder
                    val intent = Intent("com.flort.evlilik.LOGOUT")
                    context.sendBroadcast(intent)
                } catch (e: Exception) {
                    // Local temizleme hatası da görmezden gelinir
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Çıkış yapıldı")
                    }
                    val intent = Intent("com.flort.evlilik.LOGOUT")
                    context.sendBroadcast(intent)
                } finally {
                    isLoading.value = false
                }
            }
        }
    }
    
    // Refresh listener'ı set et
    LaunchedEffect(Unit) {
        SettingsActivity.refreshListener = {
            // User.current'ı güncelle
            coroutineScope.launch {
                try {
                    val success = User.updateCurrentUser(context)
                    if (success) {
                        println("AccountScreen: User.current başarıyla güncellendi")
                        // State'i güncelle
                        currentUser = User.current
                    } else {
                        println("AccountScreen: User.current güncellenemedi")
                    }
                } catch (e: Exception) {
                    println("AccountScreen: User.current güncelleme hatası: ${e.message}")
                }
            }
        }
    }

    when {
        showBlocked.value -> {
            com.flort.evlilik.modules.main.tabs.BlockedScreen()
            // Geri butonu için üstte bir AppBar veya floating button ekleyebilirsin
        }
        showContact.value -> {
            com.flort.evlilik.modules.main.tabs.ContactUsScreen()
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
            ) {
                // Üst profil
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(96.dp)) {
                        Surface(shape = CircleShape, modifier = Modifier.matchParentSize(), color = Color.LightGray) {}

                        AsyncImage(
                            model = currentUser?.getPp(),
                            contentDescription = "Profil fotoğrafı",
                            modifier = Modifier.matchParentSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.temp_pp),
                            error = painterResource(R.drawable.temp_pp)
                        )

                        // VIP çerçevesi (Lottie) - kullanıcı premium ise
                        if (currentUser?.isPremium() == true) {
                            val vipComposition = rememberLottieComposition(
                                LottieCompositionSpec.RawRes(R.raw.vip_frame)
                            ).value
                            LottieAnimation(
                                composition = vipComposition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.matchParentSize().graphicsLayer {
                                    scaleX = 2f
                                    scaleY = 2f
                                }
                            )
                        }

                        // Sağ alt düzenleme ikonu
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clickable { SettingsActivity.startGallery(context) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.profile_camera_icon),
                                    contentDescription = "Kamera",
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // İsim ve yaş
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    val name = currentUser?.name ?: "Kullanıcı"
                    val age = currentUser?.age?.toString() ?: ""
                    val displayText = if (age.isNotEmpty()) "$name, $age" else name
                    
                    Text(
                        text = displayText, 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // İki kart: Premium ve Cüzdan
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sol kart: Premium Lottie animasyonu
                    Card(
                        onClick = {
                            val intent = Intent(context, com.flort.evlilik.modules.wallet.VipActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(96.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.premium)).value
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Premium", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                    Card(
                        onClick = {
                            val intent = Intent(context, com.flort.evlilik.modules.wallet.WalletActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f).height(96.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.wallet),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Cüzdanım", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Hakkımda kartı
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hakkımda",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (!isEditingAbout) {
                                IconButton(
                                    onClick = { isEditingAbout = true }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.menu_item_2_selected),
                                        contentDescription = "Düzenle",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isEditingAbout) {
                            // Düzenleme modu
                            OutlinedTextField(
                                value = aboutText,
                                onValueChange = { aboutText = it },
                                label = { Text("Hakkımda") },
                                placeholder = { Text("Hakkımda bilginizi yazın...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorResource(id = R.color.primaryColor),
                                    unfocusedBorderColor = colorResource(id = R.color.whiteButtonStrokeColor),
                                    cursorColor = colorResource(id = R.color.primaryColor),
                                    focusedLabelColor = colorResource(id = R.color.primaryColor),
                                    unfocusedLabelColor = Color.Black.copy(alpha = 0.6f),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { 
                                        isEditingAbout = false
                                        aboutText = currentUser?.desc ?: ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Gray
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("İptal", color = Color.White)
                                }
                                
                                Button(
                                    onClick = { updateAboutText() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFA500)
                                    )
                                ) {
                                    Text("Kaydet", color = Color.White)
                                }
                            }
                        } else {
                            // Görüntüleme modu
                            if (aboutText.isNotEmpty()) {
                                Text(
                                    text = aboutText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black
                                )
                            } else {
                                Text(
                                    text = "Hakkımda bilginizi ekleyin...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.clickable { isEditingAbout = true }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bölümler ve listeler
                Column(modifier = Modifier.fillMaxWidth()) {
                    Section {
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_1,
                            title = "Profili Güncelle",
                            onClick = { SettingsActivity.startUpdateProfile(context) }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.profile_camera_icon,
                            title = "Galeri",
                            onClick = { SettingsActivity.startGallery(context) }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_5,
                            title = "Güvenlik",
                            onClick = { SettingsActivity.startSecurity(context) }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_3,
                            title = "Engellenenler",
                            onClick = { SettingsActivity.startBlockedUsers(context) }
                        )
                    }

                    Section {
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_4,
                            title = "Aboneliği Yönet",
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/account/subscriptions")
                                        setPackage("com.android.vending")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Google Play Store yüklü değilse web tarayıcısında aç
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("https://play.google.com/store/account/subscriptions")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_8,
                            title = "Bize Ulaş",
                            onClick = { SettingsActivity.startContactUs(context) }
                        )
                    }

                    Section {
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_6, 
                            title = "Gizlilik Politikası",
                            onClick = {
                                TermsActivity.start(
                                    context = context,
                                    url = Routes.BASE_URL + Routes.PRIVACY,
                                    title = "Gizlilik Politikası"
                                )
                            }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.profile_menu_icon_7, 
                            title = "Kullanım Sözleşmesi",
                            onClick = {
                                TermsActivity.start(
                                    context = context,
                                    url = Routes.BASE_URL + Routes.TERMS,
                                    title = "Kullanım Sözleşmesi"
                                )
                            }
                        )
                    }

                    Section {
                        AccountListItemView(
                            iconRes = R.drawable.trash_icon, 
                            title = "Hesabımı Sil",
                            onClick = { SettingsActivity.startDeleteAccount(context) }
                        )
                        AccountListItemView(
                            iconRes = R.drawable.logout, 
                            title = "Çıkış Yap",
                            onClick = { performLogout() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

