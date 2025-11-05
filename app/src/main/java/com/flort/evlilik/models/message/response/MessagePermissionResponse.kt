package com.flort.evlilik.models.message.response

import com.flort.evlilik.models.message.MessagePermissionData

data class MessagePermissionResponse(
    val success: Boolean,
    val data: MessagePermissionData
)
