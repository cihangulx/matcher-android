package com.flort.evlilik.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.flort.evlilik.models.message.core.Conversation
import java.text.SimpleDateFormat
import java.util.Locale
import com.flort.evlilik.R
import androidx.compose.material3.Icon

@Composable
fun ConversationItemView(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit
) {
    val otherUser = conversation.getOtherUser(currentUserId)
    val lastMessage = conversation.lastMessage
    val hasUnread = conversation.unreadCount > 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profil fotoğrafı
        Box {
            if (!otherUser?.getPhotoUrl().isNullOrEmpty()) {
                SubcomposeAsyncImage(
                    model = otherUser?.getPhotoUrl(),
                    contentDescription = "Profil Fotoğrafı",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    loading = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Gray
                    )
                }
            }
            
            // Online durumu göstergesi (opsiyonel)
            if (otherUser?.isOnline == true) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // İsim, mesaj ve zaman
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // İsim ve zaman
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherUser?.name ?: "Kullanıcı",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Zaman
                Text(
                    text = formatTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasUnread) colorResource(id = R.color.primaryColor) else Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Son mesaj ve okunmamış sayısı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Son mesaj
                val messageText = if (lastMessage != null) {
                    val prefix = if (lastMessage.senderId == currentUserId) "Sen: " else ""
                    val content = when {
                        lastMessage.type == "image" -> "📷 Fotoğraf"
                        else -> lastMessage.content ?: ""
                    }
                    prefix + content
                } else {
                    "Henüz mesaj yok"
                }
                
                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasUnread) Color.Black else Color.Gray,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Okunmamış sayısı
                if (hasUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.primaryColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Zamanı formatla (Bugün, Dün, Tarih)
 */
private fun formatTime(timestamp: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = dateFormat.parse(timestamp) ?: return ""
        
        val now = System.currentTimeMillis()
        val messageTime = date.time
        val diff = now - messageTime
        
        val oneDay = 24 * 60 * 60 * 1000L
        val oneWeek = 7 * oneDay
        
        when {
            diff < 60 * 1000 -> "Şimdi"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} dk"
            diff < oneDay -> {
                val hours = diff / (60 * 60 * 1000)
                if (hours < 24) "${hours} sa" else "Bugün"
            }
            diff < 2 * oneDay -> "Dün"
            diff < oneWeek -> {
                val timeFormat = SimpleDateFormat("EEEE", Locale("tr"))
                timeFormat.format(date)
            }
            else -> {
                val dateOnlyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                dateOnlyFormat.format(date)
            }
        }
    } catch (e: Exception) {
        ""
    }
}

