package com.matcher.matcher.models.message.response

data class UnreadCountResponse(
    val success: Boolean,
    val totalUnread: Int,
    val conversationCount: Int
)
