package com.flort.evlilik.modules.main.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.flort.evlilik.models.message.core.Message
import java.util.Locale

/**
 * Chat ekranında tarih header'ı
 * @param date Tarih (ISO 8601 string)
 */
@Composable
fun ChatDateHeader(date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = formatDateHeader(date),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }
    }
}

/**
 * Tarih header'ını formatla
 * Bugün -> "Bugün"
 * Dün -> "Dün"
 * Bu yıl -> "12 Mayıs"
 * Eski -> "12 Mayıs 2024"
 */
fun formatDateHeader(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val messageDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        
        when {
            messageDate.isEqual(today) -> {
                "Bugün"
            }
            messageDate.isEqual(today.minusDays(1)) -> {
                "Dün"
            }
            messageDate.year == today.year -> {
                // Bu yıl: "12 Mayıs"
                messageDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("tr")))
            }
            else -> {
                // Geçmiş yıl: "12 Mayıs 2024"
                messageDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")))
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}

/**
 * Mesajları tarihe göre grupla
 * @param messages Mesaj listesi
 * @return Map<DateKey, List<Message>> - DateKey günün başlangıcı
 */
fun groupMessagesByDate(messages: List<Message>): Map<String, List<Message>> {
    return messages.groupBy { message ->
        try {
            val instant = Instant.parse(message.createdAt)
            val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate()
            // Günün başlangıcını key olarak kullan
            localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toString()
        } catch (e: Exception) {
            message.createdAt // Fallback
        }
    }
}

