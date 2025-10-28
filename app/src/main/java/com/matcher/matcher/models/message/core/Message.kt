package com.matcher.matcher.models.message.core

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("_id") val id: String? = null,
    val tempId: String? = null,
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String? = null,
    val senderPhoto: String? = null,
    val receiverName: String? = null,
    val receiverPhoto: String? = null,
    val content: String = "",
    val type: String = "text",
    val mediaUrl: String? = null,
    val status: String = "sending",
    val failReason: String? = null,
    val deliveredAt: String? = null,
    val readAt: String? = null,
    val replyTo: ReplyMessage? = null,
    val giftId: String? = null,
    val createdAt: String = "",
    val updatedAt: String? = null
) {
    fun isMine(currentUserId: String): Boolean = senderId == currentUserId
    
    fun getStatusEnum(): MessageStatus {
        return when (status.lowercase()) {
            "sending" -> MessageStatus.SENDING
            "sent" -> MessageStatus.SENT
            "delivered" -> MessageStatus.DELIVERED
            "read" -> MessageStatus.READ
            "failed" -> MessageStatus.FAILED
            else -> MessageStatus.SENDING
        }
    }
}
