package com.flort.evlilik.models.message.socket

import com.flort.evlilik.models.message.core.Message

data class SocketMessageEvent(
    val message: Message,
    val conversationId: String
)
