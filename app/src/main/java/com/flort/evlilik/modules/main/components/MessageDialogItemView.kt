package com.flort.evlilik.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flort.evlilik.models.message.core.Conversation
import com.flort.evlilik.models.message.core.Message
import com.flort.evlilik.models.message.ConversationUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDialogDateLabel(timestamp: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(timestamp)
        val now = Date()
        val diff = now.time - (date?.time ?: 0)
        
        when {
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date ?: Date())
            diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date ?: Date())
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date ?: Date())
        }
    } catch (e: Exception) {
        timestamp
    }
}

@Composable
fun MessageDialogItemView(
    dialog: Conversation,
    currentUserId: String? = null,
    modifier: Modifier = Modifier,
    onClick: (Conversation) -> Unit = {}
) {
    val otherUser: ConversationUser? = dialog.otherUser
    val lastMessage: Message? = dialog.lastMessage
    val unreadCount: Int = dialog.unreadCount

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(dialog) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        BadgedBox(badge = {
            if (unreadCount > 0) {
                Badge(containerColor = Color(0xFFEF4444)) {
                    Text(text = unreadCount.coerceAtMost(99).toString(), color = Color.White, fontSize = 10.sp)
                }
            }
        }) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.LightGray
            ) {
                if (otherUser?.getPhotoUrl() != null) {
                    AsyncImage(
                        model = otherUser.getPhotoUrl(),
                        contentDescription = null,
                        modifier = Modifier.clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.background(Color(0xFFE5E7EB)))
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = otherUser?.name ?: "Bilinmeyen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (unreadCount > 0) FontWeight.SemiBold else FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (lastMessage?.createdAt != null) {
                    Text(
                        text = formatDialogDateLabel(lastMessage.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = lastMessage?.content ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = if (unreadCount > 0) Color.Black else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Divider
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.LightGray.copy(alpha = 0.3f))
    )
}
