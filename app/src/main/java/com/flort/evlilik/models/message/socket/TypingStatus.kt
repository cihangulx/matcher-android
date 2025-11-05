package com.flort.evlilik.models.message.socket

data class TypingStatus(
    val userId: String,
    val isTyping: Boolean
)
