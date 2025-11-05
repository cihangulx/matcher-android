package com.flort.evlilik.models.message.response

import com.flort.evlilik.models.message.MessageCostsData

data class MessageCostsResponse(
    val success: Boolean,
    val data: MessageCostsData
)
