package com.matcher.matcher.modules.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matcher.matcher.R
import com.matcher.matcher.modules.account.components.SecuritySection
import com.matcher.matcher.modules.account.components.SecurityListItemView
import com.matcher.matcher.modules.account.components.SecuritySwitchItemView

@Composable
fun SecurityScreen(
    onBack: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.page_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        val scrollState = rememberScrollState()
        
        val profileVisibleToEveryone = remember { mutableStateOf(true) }
        val allowNotifications = remember { mutableStateOf(true) }
        val allowMessagesFromOthers = remember { mutableStateOf(true) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Güvenlik",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Hesap güvenliğin ve gizlilik ayarlarını yönet.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                SecuritySection {
                    SecurityListItemView(
                        iconRes = R.drawable.profile_menu_icon_5,
                        title = "Şifremi Değiştir",
                        onClick = { }
                    )
                }

                SecuritySection {
                    SecuritySwitchItemView(
                        iconRes = R.drawable.profile_menu_icon_1,
                        title = "Profilim herkese görünsün",
                        subtitle = "Profilin diğer kullanıcılara görünür olacak",
                        checked = profileVisibleToEveryone.value,
                        onCheckedChange = { profileVisibleToEveryone.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecuritySwitchItemView(
                        iconRes = R.drawable.profile_menu_icon_1,
                        title = "Bildirimlere izin ver",
                        subtitle = "Uygulama bildirimlerini al",
                        checked = allowNotifications.value,
                        onCheckedChange = { allowNotifications.value = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecuritySwitchItemView(
                        iconRes = R.drawable.profile_menu_icon_1,
                        title = "Başkası bana mesaj gönderebilir",
                        subtitle = "Diğer kullanıcılar sana mesaj gönderebilir",
                        checked = allowMessagesFromOthers.value,
                        onCheckedChange = { allowMessagesFromOthers.value = it }
                    )
                }

                SecuritySection {
                    SecurityListItemView(
                        iconRes = R.drawable.profile_menu_icon_5,
                        title = "İki faktörlü doğrulama",
                        onClick = { }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SecurityListItemView(
                        iconRes = R.drawable.profile_menu_icon_5,
                        title = "Oturum geçmişi",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


