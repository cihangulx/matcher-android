package com.flort.evlilik.models.message.response

import com.flort.evlilik.models.message.core.Conversation

data class ConversationResponse(
    val success: Boolean,
    val conversation: Conversation
)
