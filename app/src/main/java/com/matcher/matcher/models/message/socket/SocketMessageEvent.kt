package com.matcher.matcher.models.message.socket

import com.matcher.matcher.models.message.core.Message

data class SocketMessageEvent(
    val message: Message,
    val conversationId: String
)
