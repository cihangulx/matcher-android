package com.matcher.matcher.modules.main.message

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.matcher.matcher.R
import com.matcher.matcher.models.message.core.Message
import com.matcher.matcher.models.message.core.MessageStatus
import com.matcher.matcher.models.message.core.ReplyMessage
import com.airbnb.lottie.compose.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Tek bir mesaj item'ı
 * @param message Mesaj objesi
 * @param isMine Bu mesaj benim mi?
 * @param showProfilePhoto Profil fotoğrafı göster mi? (grup mesajlarında kullanılabilir)
 * @param onImageClick Resim tıklandığında
 * @param onResendClick Tekrar gönder tıklandığında
 * @param onDeleteClick Mesaj silindiğinde
 * @param onReportClick Şikayet edildiğinde (karşı tarafın mesajları için)
 * @param onReplyClick Reply tıklandığında
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: Message,
    isMine: Boolean,
    showProfilePhoto: Boolean = false,
    onImageClick: ((String) -> Unit)? = null,
    onResendClick: ((Message) -> Unit)? = null,
    onDeleteClick: ((Message) -> Unit)? = null,
    onReportClick: ((Message) -> Unit)? = null,
    onReplyClick: ((Message) -> Unit)? = null
) {
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val backgroundColor = if (isMine) 
        colorResource(id = R.color.primaryColor) 
    else 
        Color(0xFFF0F0F0)
    val textColor = if (isMine) Color.White else Color.Black
    
    // Dialog state'leri
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    
    // Animasyon state'leri
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    
    // Animasyon değerleri
    val animatedOffset by animateFloatAsState(
        targetValue = if (isDragging) dragOffset else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "dragOffset"
    )
    
    val replyThreshold = 80f // Reply tetikleme eşiği
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = animatedOffset
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true
                        },
                        onDragEnd = { 
                            isDragging = false
                            if (dragOffset > replyThreshold && onReplyClick != null) {
                                // Reply tetikleme
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onReplyClick(message)
                            }
                            dragOffset = 0f
                        }
                    ) { change, dragAmount ->
                        // Sadece sağa kaydırmaya izin ver
                        if (dragAmount.x > 0) {
                            dragOffset = (dragOffset + dragAmount.x).coerceAtMost(120f)
                            
                            // Eşik geçildiğinde titreşim
                            if (dragOffset > replyThreshold && dragOffset - dragAmount.x <= replyThreshold) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }
                    }
                },
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tekrar gönder butonu (sadece benim başarısız mesajlarım için)
            if (isMine && message.getStatusEnum() == MessageStatus.FAILED && onResendClick != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE))
                        .clickable { onResendClick(message) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.resend_icon),
                        contentDescription = "Tekrar Gönder",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFD32F2F)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Profil fotoğrafı (sadece karşı taraf için)
            if (!isMine && showProfilePhoto) {
                // TODO: Profil fotoğrafı eklenebilir
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(
                horizontalAlignment = alignment,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                // Ana mesaj container'ı (reply + mesaj içeriği birlikte)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        ))
                        .background(if (message.type == "image" && message.giftId != null) Color.Transparent else backgroundColor)
                        .combinedClickable(
                            onClick = { /* Normal tıklama - şimdilik boş */ },
                            onLongClick = {
                                if (isMine && onDeleteClick != null) {
                                    // Kendi mesajımız - sil
                                    showDeleteDialog = true
                                } else if (!isMine && onReportClick != null) {
                                    // Karşı tarafın mesajı - şikayet et
                                    showReportDialog = true
                                }
                            }
                        )
                        .padding(
                            horizontal = if (message.type == "image") 8.dp else 12.dp,
                            vertical = if (message.type == "image") 8.dp else 8.dp
                        )
                ) {
                    Column {
                        // Reply mesajı varsa göster (ana container içinde)
                        if (message.replyTo != null) {
                            ReplyPreview(
                                replyMessage = message.replyTo,
                                isMine = isMine,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        } else {
                        }
                        
                        // Mesaj içeriği
                        // Hediye mesajı mı kontrol et - önce hediye kontrolü
                        if (message.type == "image" && message.giftId != null) {
                            // Hediye mesajı - özel tasarım, arkaplan yok
                            GiftMessageContent(
                                message = message,
                                isMine = isMine,
                                onImageClick = onImageClick,
                                onLongClick = {
                                    if (isMine && onDeleteClick != null) {
                                        showDeleteDialog = true
                                    } else if (!isMine && onReportClick != null) {
                                        showReportDialog = true
                                    }
                                }
                            )
                        } else {
                            // Normal mesajlar
                            when (message.type) {
                                "text" -> {
                                    Text(
                                        text = message.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                }
                                "image" -> {
                                    // Normal resim mesajı - stroke yok, sadece resim
                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 250.dp)
                                            .heightIn(max = 300.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .combinedClickable(
                                                onClick = { 
                                                    message.mediaUrl?.let { url -> 
                                                        onImageClick?.invoke(url) 
                                                    } 
                                                },
                                                onLongClick = {
                                                    if (isMine && onDeleteClick != null) {
                                                        // Kendi mesajımız - sil
                                                        showDeleteDialog = true
                                                    } else if (!isMine && onReportClick != null) {
                                                        // Karşı tarafın mesajı - şikayet et
                                                        showReportDialog = true
                                                    }
                                                }
                                            )
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = message.mediaUrl,
                                            contentDescription = "Mesaj resmi",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.Crop
                                        )
                                        
                                        // Resim altında caption gösterilmez (hem kendi hem gelen resimlerde)
                                    }
                                }
                                else -> {
                                    // Desteklenmeyen mesaj tipi
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.LightGray)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Desteklenmeyen mesaj tipi",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                // Saat ve durum
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    // Saat
                    Text(
                        text = formatMessageTime(message.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    
                    // Mesaj durumu (sadece kendi mesajlarımız için)
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.getStatusEnum())
                    }
                }
            }
        }
    }
    
    // Silme onay dialog'u
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Mesajı Sil",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu mesajı silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick?.invoke(message)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal", color = colorResource(id = R.color.primaryColor))
                }
            },
            containerColor = Color.White
        )
    }
    
    // Şikayet et onay dialog'u
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "Kullanıcıyı Şikayet Et",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bu kullanıcıyı şikayet etmek istiyor musunuz? Şikayet detaylarını belirtebileceğiniz sayfaya yönlendirileceksiniz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        onReportClick?.invoke(message)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6F00)
                    )
                ) {
                    Text("Şikayet Et", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("İptal", color = colorResource(id = R.color.primaryColor))
                }
            },
            containerColor = Color.White
        )
    }
}

/**
 * Reply mesajı preview component'i (WhatsApp tarzı - ana container içinde)
 */
@Composable
private fun ReplyPreview(
    replyMessage: ReplyMessage,
    isMine: Boolean,
    modifier: Modifier = Modifier
) {
    // Ana container ile aynı arka plan rengi (şeffaf)
    val replyBackgroundColor = if (isMine) 
        Color.White.copy(alpha = 0.2f)
    else 
        Color.Black.copy(alpha = 0.1f)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = replyBackgroundColor,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Sol taraftaki renkli çizgi (WhatsApp benzeri) - daha belirgin
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .background(
                    color = if (isMine) colorResource(id = R.color.primaryDarkColor) else colorResource(id = R.color.primaryDarkColor),
                    shape = RoundedCornerShape(2.dp)
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Reply başlığı - daha belirgin
            Text(
                text = replyMessage.senderName ?: "Kullanıcı Adı",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMine) Color.White else colorResource(id = R.color.primaryColor),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            
            Spacer(modifier = Modifier.height(1.dp))
            
            // Reply mesaj içeriği
            when (replyMessage.type) {
                "text" -> {
                    Text(
                        text = replyMessage.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMine) Color.White.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
                "image" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.attach_icon),
                            contentDescription = "Resim",
                            modifier = Modifier.size(12.dp),
                            tint = if (isMine) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "📷 Resim",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isMine) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Desteklenmeyen mesaj tipi",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMine) Color.White.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Mesaj durumu ikonu (WhatsApp tarzı)
 */
@Composable
private fun MessageStatusIcon(status: MessageStatus) {
    val (icon, tint) = when (status) {
        MessageStatus.SENDING -> R.drawable.clock_watch_icon to Color.Gray
        MessageStatus.SENT -> R.drawable.check_mark_icon to Color.Gray
        MessageStatus.DELIVERED -> R.drawable.double_check_mark_icon to Color.Gray
        MessageStatus.READ -> R.drawable.blue_double_check_mark_icon to colorResource(id = R.color.primaryColor)
        MessageStatus.FAILED -> R.drawable.ic_message_failed to Color.Red
    }

    Icon(
        painter = painterResource(id = icon),
        contentDescription = "Mesaj durumu",
        modifier = Modifier.size(14.dp),
        tint = tint
    )
}


/**
 * Hediye mesajı için özel tasarım - arkaplan yok, sadece hediye içeriği
 */
@Composable
private fun GiftMessageContent(
    message: Message,
    isMine: Boolean,
    onImageClick: ((String) -> Unit)?,
    onLongClick: () -> Unit
) {
    // Hediye mesajı için özel tasarım - sadece resim, hiç arkaplan yok
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { 
                    message.mediaUrl?.let { url -> 
                        onImageClick?.invoke(url) 
                    } 
                },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!message.mediaUrl.isNullOrEmpty()) {
            SubcomposeAsyncImage(
                model = message.mediaUrl,
                contentDescription = "Hediye",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            // Resim yoksa Lottie animasyonu göster
            val composition = rememberLottieComposition(
                LottieCompositionSpec.RawRes(com.matcher.matcher.R.raw.gift)
            ).value
            
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(100.dp)
            )
        }
    }
}

/**
 * Mesaj zamanını formatla (sadece saat:dakika)
 */
private fun formatMessageTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        localDateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale("tr")))
    } catch (e: Exception) {
        ""
    }
}