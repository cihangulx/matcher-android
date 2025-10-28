package com.matcher.matcher.modules.account

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.matcher.matcher.R
import com.matcher.matcher.models.ticket.RemoveMessage
import com.matcher.matcher.models.user.User
import com.matcher.matcher.models.user.SecuritySettings
import com.matcher.matcher.models.user.request.UpdateSecuritySettingsRequest
import com.matcher.matcher.models.user.request.ChangePasswordRequest
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_BLOCKED_USERS
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_CHANGE_PASSWORD
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_CONTACT_US
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_DELETE_ACCOUNT
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_GALLERY
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_SECURITY
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_UPDATE_PROFILE
import com.matcher.matcher.modules.account.SettingsActivity.Companion.TYPE_UPDATE_PROFILE_SETTINGS
import com.matcher.matcher.modules.account.components.SecuritySection
import com.matcher.matcher.modules.account.components.SecurityListItemView
import com.matcher.matcher.modules.account.components.SecuritySwitchItemView
import com.matcher.matcher.modules.main.tabs.ContactUsScreen
import com.matcher.matcher.modules.auth.components.Gender
import com.matcher.matcher.network.ApiClient
import com.matcher.matcher.network.model.ApiException
import com.matcher.matcher.utils.helpers.ToastHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            
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
            
            when (settingType) {
                TYPE_SECURITY -> SecurityContent()
                TYPE_UPDATE_PROFILE -> UpdateProfileContent()
                TYPE_GALLERY -> GalleryContent()
                TYPE_CONTACT_US -> ContactUsContent()
                TYPE_UPDATE_PROFILE_SETTINGS -> UpdateProfileSettingsContent()
                TYPE_DELETE_ACCOUNT -> DeleteAccountContent()
                TYPE_CHANGE_PASSWORD -> ChangePasswordContent()
                TYPE_BLOCKED_USERS -> BlockedUsersContent()
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.left_arrow),
            contentDescription = "Geri",
            modifier = Modifier
                .size(24.dp)
                .clickable { onBack() },
            tint = Color.Black
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Update Profile Settings Content - To be implemented")
    }
}

@Composable
private fun DeleteAccountContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Delete Account Content - To be implemented")
    }
}

@Composable
private fun ChangePasswordContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Change Password Content - To be implemented")
    }
}

@Composable
private fun BlockedUsersContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Blocked Users Content - To be implemented")
    }
}
