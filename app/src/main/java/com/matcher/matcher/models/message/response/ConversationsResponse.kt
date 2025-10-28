package com.matcher.matcher.models.message.response

import com.matcher.matcher.models.message.core.Conversation

data class ConversationsResponse(
    val success: Boolean,
    val conversations: List<Conversation>,
    val page: Int,
    val limit: Int,
    val total: Int? = null,
    val hasMore: Boolean? = null
)
