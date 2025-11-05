package com.flort.evlilik.modules.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.flort.evlilik.R
import com.flort.evlilik.models.message.core.ReplyMessage

@Composable
fun MessageInputView(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leftIcon: ImageVector? = null,
    onLeftIconClick: (() -> Unit)? = null,
    rightIcon: ImageVector? = null,
    onRightIconClick: (() -> Unit)? = null,
    placeholder: String = "Mesaj yaz...",
    replyMessage: ReplyMessage? = null,
    onReplyCancel: (() -> Unit)? = null,
    keepFocus: Boolean = true
) {
    val hasText = value.isNotEmpty()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            // Reply preview
            if (replyMessage != null) {
                ReplyInputPreview(
                    replyMessage = replyMessage,
                    onCancel = onReplyCancel
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Sol taraf - Attach butonu
                Icon(
                    painter = painterResource(id = R.drawable.attach_icon),
                    contentDescription = "Dosya ekle",
                    modifier = Modifier
                        .size(36.dp)
                        .padding(bottom = 6.dp)
                        .clickable(enabled = enabled) { 
                            onLeftIconClick?.invoke()
                        },
                    tint = if (enabled) Color.Gray else Color.LightGray
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Orta - Text Input
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = true, // Her zaman enabled - focus kaybını önlemek için
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    placeholder = { 
                        Text(
                            text = placeholder,
                            color = Color.Gray
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFE0E0E0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (hasText && enabled) {
                                onRightIconClick?.invoke()
                                // Focus'u koru - klavyeyi kapatma
                                if (keepFocus) {
                                    // Klavyeyi açık tut
                                    keyboardController?.show()
                                }
                            }
                        }
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Sağ taraf - Gönder butonu
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (hasText && enabled) 
                                colorResource(id = R.color.primaryColor) 
                            else 
                                Color(0xFFE0E0E0)
                        )
                        .clickable(enabled = hasText && enabled) { 
                            if (hasText && enabled) {
                                onRightIconClick?.invoke()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (hasText && enabled) 
                                R.drawable.message_send_selected 
                            else 
                                R.drawable.message_send_icon
                        ),
                        contentDescription = "Gönder",
                        modifier = Modifier.size(24.dp),
                        tint = if (hasText && enabled) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Reply input preview component
 */
@Composable
private fun ReplyInputPreview(
    replyMessage: ReplyMessage,
    onCancel: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol çizgi
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(
                        color = colorResource(id = R.color.primaryColor),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Reply içeriği
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "↩ ${replyMessage.senderName ?: "Kullanıcı"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colorResource(id = R.color.primaryColor),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                when (replyMessage.type) {
                    "text" -> {
                        Text(
                            text = replyMessage.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    "image" -> {
                        Text(
                            text = "📷 Resim",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                    else -> {
                        Text(
                            text = "Desteklenmeyen mesaj tipi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // İptal butonu
            if (onCancel != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_close_small_24),
                        contentDescription = "Kapat",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}