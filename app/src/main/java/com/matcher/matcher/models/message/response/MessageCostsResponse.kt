package com.matcher.matcher.models.message.response

import com.matcher.matcher.models.message.MessageCostsData

data class MessageCostsResponse(
    val success: Boolean,
    val data: MessageCostsData
)
