package com.flort.evlilik.models.message

data class MessagePermissionData(
    val canSendMessage: Boolean,
    val currentBalance: Int,
    val requiredTokens: Int,
    val remainingBalance: Int,
    val messageType: String,
    val allCosts: MessageCosts
)
