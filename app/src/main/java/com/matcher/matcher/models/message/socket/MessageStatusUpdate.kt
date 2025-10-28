package com.matcher.matcher.models.message.socket

data class MessageStatusUpdate(
    val messageId: String,
    val status: String,
    val tempId: String? = null,
    val deliveredAt: String? = null,
    val readAt: String? = null,
    val failReason: String? = null
)
