package com.flort.evlilik.models.message.response

import com.flort.evlilik.models.message.core.Message

data class MessagesResponse(
    val success: Boolean,
    val messages: List<Message>,
    val conversationId: String,
    val page: Int,
    val limit: Int,
    val total: Int,
    val hasMore: Boolean
)
