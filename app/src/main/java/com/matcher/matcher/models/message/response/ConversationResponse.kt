package com.matcher.matcher.models.message.response

import com.matcher.matcher.models.message.core.Conversation

data class ConversationResponse(
    val success: Boolean,
    val conversation: Conversation
)
