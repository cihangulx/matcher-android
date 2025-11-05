package com.flort.evlilik.modules.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flort.evlilik.components.GalleryContent
import com.flort.evlilik.models.user.User
import com.flort.evlilik.models.user.SecuritySettings
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_BLOCKED_USERS
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_CHANGE_PASSWORD
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_CONTACT_US
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_DELETE_ACCOUNT
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_GALLERY
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_SECURITY
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_UPDATE_PROFILE
import com.flort.evlilik.modules.account.SettingsActivity.Companion.TYPE_UPDATE_PROFILE_SETTINGS
import com.flort.evlilik.modules.account.components.SecuritySection
import com.flort.evlilik.modules.account.components.SecuritySwitchItemView
import com.flort.evlilik.modules.main.tabs.ContactUsScreen
import com.flort.evlilik.modules.auth.components.Gender
import com.flort.evlilik.utils.helpers.ToastHelper
import com.flort.evlilik.R
import com.flort.evlilik.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.colorResource

class SettingsActivity : ComponentActivity() {
    companion object {
        const val EXTRA_SETTING_TYPE = "setting_type"
        const val TYPE_SECURITY = "security"
        const val TYPE_UPDATE_PROFILE = "update_profile"
        const val TYPE_GALLERY = "gallery"
        const val TYPE_CONTACT_US = "contact_us"
        const val TYPE_UPDATE_PROFILE_SETTINGS = "update_profile_settings"
        const val TYPE_DELETE_ACCOUNT = "delete_account"
        const val TYPE_CHANGE_PASSWORD = "change_password"
        const val TYPE_BLOCKED_USERS = "blocked_users"
        
        var refreshListener: (() -> Unit)? = null
        
        fun startSecurity(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_SECURITY)
            }
            context.startActivity(intent)
        }
        
        fun startUpdateProfile(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_UPDATE_PROFILE)
            }
            context.startActivity(intent)
        }
        
        fun startGallery(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_GALLERY)
            }
            context.startActivity(intent)
        }
        
        fun startContactUs(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_CONTACT_US)
            }
            context.startActivity(intent)
        }
        
        fun startUpdateProfileSettings(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_UPDATE_PROFILE_SETTINGS)
            }
            context.startActivity(intent)
        }
        
        fun startDeleteAccount(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_DELETE_ACCOUNT)
            }
            context.startActivity(intent)
        }
        
        fun startChangePassword(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_CHANGE_PASSWORD)
            }
            context.startActivity(intent)
        }
        
        fun startBlockedUsers(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(EXTRA_SETTING_TYPE, TYPE_BLOCKED_USERS)
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val settingType = intent.getStringExtra(EXTRA_SETTING_TYPE) ?: TYPE_SECURITY
        
        setContent {
            SettingsScreen(
                settingType = settingType,
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    settingType: String,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Kendi scroll'unu kullanan sayfalar için scroll gereksiz
        val needsScroll = settingType !in listOf(TYPE_GALLERY, TYPE_BLOCKED_USERS, TYPE_DELETE_ACCOUNT, TYPE_UPDATE_PROFILE)
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            
            // Toolbar her zaman scroll dışında
            SettingsToolbar(
                title = when (settingType) {
                    TYPE_SECURITY -> "Güvenlik"
                    TYPE_UPDATE_PROFILE -> "Profili Güncelle"
                    TYPE_GALLERY -> "Galeri"
                    TYPE_CONTACT_US -> "İletişim"
                    TYPE_UPDATE_PROFILE_SETTINGS -> "Profil Ayarları"
                    TYPE_DELETE_ACCOUNT -> "Hesabı Sil"
                    TYPE_CHANGE_PASSWORD -> "Şifre Değiştir"
                    TYPE_BLOCKED_USERS -> "Engellenen Kullanıcılar"
                    else -> "Ayarlar"
                },
                onBack = onBack
            )
            
            // İçerik
            if (needsScroll) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (settingType) {
                        TYPE_SECURITY -> SecurityContent()
                        TYPE_CONTACT_US -> ContactUsContent()
                        TYPE_UPDATE_PROFILE_SETTINGS -> UpdateProfileSettingsContent()
                        TYPE_CHANGE_PASSWORD -> ChangePasswordContent()
                        else -> {}
                    }
                }
            } else {
                when (settingType) {
                    TYPE_GALLERY -> GalleryContent()
                    TYPE_BLOCKED_USERS -> BlockedUsersContent()
                    TYPE_DELETE_ACCOUNT -> DeleteAccountContent()
                    TYPE_UPDATE_PROFILE -> UpdateProfileContent()
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SettingsToolbar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp), clip = false)
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.left_arrow),
            contentDescription = "Geri",
            modifier = Modifier
                .size(24.dp)
                .clickable { onBack() },
            tint = Color.Black
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        // Sağ tarafta boşluk için invisible box (sol icon ile simetri için)
        Box(modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun SecurityContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var profileVisibleToEveryone by remember { mutableStateOf(true) }
    var allowNotifications by remember { mutableStateOf(true) }
    var allowMessagesFromOthers by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        val currentUser = User.current
        val securitySettings = currentUser?.securitySettings
        if (securitySettings != null) {
            profileVisibleToEveryone = securitySettings.profileVisible
            allowNotifications = securitySettings.allowNotifications
            allowMessagesFromOthers = securitySettings.allowMessages
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        SecuritySection {
            SecuritySwitchItemView(
                iconRes = R.drawable.profile_menu_icon_5,
                title = "Profil Herkese Görünür",
                subtitle = "Profilinizi herkesin görmesine izin verin",
                checked = profileVisibleToEveryone,
                onCheckedChange = { isChecked ->
                    profileVisibleToEveryone = isChecked
                    updateSecuritySetting(context, coroutineScope, "profileVisible", isChecked)
                }
            )
        }
        
        SecuritySection {
            SecuritySwitchItemView(
                iconRes = R.drawable.profile_menu_icon_1,
                title = "Bildirimlere İzin Ver",
                subtitle = "Yeni mesaj ve beğeni bildirimlerini alın",
                checked = allowNotifications,
                onCheckedChange = { isChecked ->
                    allowNotifications = isChecked
                    updateSecuritySetting(context, coroutineScope, "allowNotifications", isChecked)
                }
            )
        }
        
        SecuritySection {
            SecuritySwitchItemView(
                iconRes = R.drawable.profile_menu_icon_1,
                title = "Mesajlara İzin Ver",
                subtitle = "Diğer kullanıcılardan mesaj alın",
                checked = allowMessagesFromOthers,
                onCheckedChange = { isChecked ->
                    allowMessagesFromOthers = isChecked
                    updateSecuritySetting(context, coroutineScope, "allowMessages", isChecked)
                }
            )
        }
    }
}

private fun updateSecuritySetting(
    context: Context,
    coroutineScope: CoroutineScope,
    setting: String,
    value: Boolean
) {
    coroutineScope.launch {
        try {
            val currentUser = User.current
            if (currentUser != null) {
                val currentSettings = currentUser.securitySettings ?: SecuritySettings(
                    profileVisible = true,
                    allowNotifications = true,
                    allowMessages = true
                )

                val updatedSettings = when (setting) {
                    "profileVisible" -> currentSettings.copy(profileVisible = value)
                    "allowNotifications" -> currentSettings.copy(allowNotifications = value)
                    "allowMessages" -> currentSettings.copy(allowMessages = value)
                    else -> currentSettings
                }
                currentUser.securitySettings = updatedSettings
            }
        } catch (e: Exception) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showError(activity, "Ayar güncellenemedi")
            }
        }
    }
}

@Composable
private fun UpdateProfileContent() {
    val context = LocalContext.current
    val currentUser = User.current
    
    val initialName = currentUser?.name ?: ""
    val initialAge = currentUser?.age?.toString() ?: ""
    val initialGender = when (currentUser?.gender) {
        1 -> Gender.MALE
        2 -> Gender.FEMALE
        else -> null
    }
    
    UpdateProfileScreen(
        onBack = { 
            SettingsActivity.refreshListener?.invoke()
            (context as ComponentActivity).finish() 
        },
        onSave = { 
            try {
                SettingsActivity.refreshListener?.invoke()
                (context as ComponentActivity).finish()
            } catch (e: Exception) {
            }
        },
        initialName = initialName,
        initialAge = initialAge,
        initialGender = initialGender
    )
}

@Composable
private fun ContactUsContent() {
    ContactUsScreen()
}

@Composable
private fun UpdateProfileSettingsContent() {
    // Profil ayarları zaten SecurityContent'te mevcut
    // Bu sayfa için şimdilik SecurityContent'i gösteriyoruz
    // İleride farklı ayarlar eklenebilir
    SecurityContent()
}

@Composable
private fun DeleteAccountContent() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // State'ler
    var removeMessages by remember { mutableStateOf<List<com.flort.evlilik.models.ticket.RemoveMessage>>(emptyList()) }
    var selectedReason by remember { mutableStateOf<com.flort.evlilik.models.ticket.RemoveMessage?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    
    // API'den hesap silme nedenlerini çek
    LaunchedEffect(Unit) {
        try {
            val ticketService = ApiClient.getInstance(context).ticketService
            val response = ticketService.getRemoveMessages()
            if (response.success && response.data != null) {
                removeMessages = response.data
            } else {
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, response.message ?: "Nedenler yüklenemedi")
                }
            }
        } catch (e: Exception) {
            val errorMessage = com.flort.evlilik.network.model.ApiException.getErrorMessage(e)
            (context as? Activity)?.let { activity ->
                ToastHelper.showError(activity, errorMessage)
            }
        } finally {
            isLoading = false
        }
    }
    
    // Hesap silme ticket gönderme fonksiyonu
    fun sendDeleteAccountTicket() {
        if (selectedReason == null) {
            (context as? Activity)?.let { activity ->
                ToastHelper.showWarning(activity, "Lütfen bir sebep seçin")
            }
            return
        }
        
        coroutineScope.launch {
            isSending = true
            try {
                val ticketService = ApiClient.getInstance(context).ticketService
                val request = com.flort.evlilik.models.ticket.TicketRequest(
                    title = "delete_account",
                    message = selectedReason!!.message
                )
                
                val response = ticketService.sendTicket(request)
                
                if (response.success) {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showSuccess(activity, "Hesap silme talebiniz başarıyla gönderildi")
                    }
                    // Ayarlar sayfasına geri dön
                    (context as? ComponentActivity)?.finish()
                } else {
                    (context as? Activity)?.let { activity ->
                        ToastHelper.showError(activity, response.message ?: "Talep gönderilemedi")
                    }
                }
            } catch (e: Exception) {
                val errorMessage = com.flort.evlilik.network.model.ApiException.getErrorMessage(e)
                (context as? Activity)?.let { activity ->
                    ToastHelper.showError(activity, errorMessage)
                }
            } finally {
                isSending = false
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Ana içerik - scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_error),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFE53935)
                )
            }
            
            // Başlık
            Text(
                text = "Hesabınızı silmek istediğinize emin misiniz?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Açıklama
            Text(
                text = "Bu işlem geri alınamaz. Lütfen silme sebebinizi seçin:",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isLoading) {
                // Loading indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = colorResource(id = R.color.primaryColor)
                    )
                }
            } else {
                // Remove messages listesi
                removeMessages.forEach { reason ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorResource(id = R.color.primaryColor),
                                    unselectedColor = Color.Gray
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = reason.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Alt boşluk - buton için yer bırak
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // Sabitlenmiş buton - alt kısımda
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.White)
                .padding(16.dp)
        ) {
            Button(
                onClick = { sendDeleteAccountTicket() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSending && selectedReason != null
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Hesabı Sil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangePasswordContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Şifre Değiştir",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Mevcut şifre
        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Mevcut Şifre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(id = if (showPassword) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                        contentDescription = if (showPassword) "Şifreyi Gizle" else "Şifreyi Göster"
                    )
                }
            },
            singleLine = true
        )
        
        // Yeni şifre
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Yeni Şifre") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            visualTransformation = if (showNewPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                    Icon(
                        painter = painterResource(id = if (showNewPassword) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                        contentDescription = if (showNewPassword) "Şifreyi Gizle" else "Şifreyi Göster"
                    )
                }
            },
            singleLine = true
        )
        
        // Yeni şifre tekrar
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Yeni Şifre Tekrar") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = if (showConfirmPassword) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        painter = painterResource(id = if (showConfirmPassword) R.drawable.ic_eye_closed else R.drawable.ic_eye),
                        contentDescription = if (showConfirmPassword) "Şifreyi Gizle" else "Şifreyi Göster"
                    )
                }
            },
            singleLine = true
        )
        
        // Kaydet butonu
        Button(
            onClick = {
                val activity = context as? Activity ?: return@Button
                if (currentPassword.isBlank()) {
                    ToastHelper.showWarning(activity, "Lütfen mevcut şifrenizi girin")
                    return@Button
                }
                if (newPassword.isBlank()) {
                    ToastHelper.showWarning(activity, "Lütfen yeni şifrenizi girin")
                    return@Button
                }
                if (newPassword.length < 6) {
                    ToastHelper.showWarning(activity, "Şifre en az 6 karakter olmalıdır")
                    return@Button
                }
                if (newPassword != confirmPassword) {
                    ToastHelper.showWarning(activity, "Yeni şifreler eşleşmiyor")
                    return@Button
                }
                
                isLoading = true
                coroutineScope.launch {
                    try {
                        val userService = ApiClient.getInstance(context).userService
                        val request = com.flort.evlilik.models.user.request.ChangePasswordRequest(
                            currentPassword = currentPassword,
                            newPassword = newPassword
                        )
                        
                        val response = userService.updatePassword(request)
                        
                        val activity = context as? Activity
                        if (response.success) {
                            activity?.let {
                                ToastHelper.showSuccess(it, "Şifre başarıyla değiştirildi")
                            }
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        } else {
                            activity?.let {
                                ToastHelper.showError(it, response.message ?: "Şifre değiştirilemedi")
                            }
                        }
                    } catch (e: Exception) {
                        (context as? Activity)?.let { activity ->
                            ToastHelper.showError(activity, "Bağlantı hatası: ${e.message}")
                        }
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.primaryColor)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Şifreyi Değiştir", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BlockedUsersContent() {
    com.flort.evlilik.modules.main.tabs.BlockedScreen()
}
