package com.flort.evlilik.models.message

import com.flort.evlilik.models.message.core.Message

sealed class SendMessageResponse {
    data class Success(
        val message: Message,
        val status: String,
        val conversationId: String
    ) : SendMessageResponse()
    
    data class Failed(
        val error: String,
        val failReason: String,
        val currentBalance: Int,
        val requiredTokens: Int
    ) : SendMessageResponse()
    
    data class Error(
        val message: String
    ) : SendMessageResponse()
}
