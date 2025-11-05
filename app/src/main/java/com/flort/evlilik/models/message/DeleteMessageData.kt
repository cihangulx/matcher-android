package com.flort.evlilik.models.message

data class DeleteMessageData(
    val messageId: String,
    val conversationId: String
)
