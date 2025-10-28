package com.matcher.matcher.models.message.response

import com.matcher.matcher.models.message.MessagePermissionData

data class MessagePermissionResponse(
    val success: Boolean,
    val data: MessagePermissionData
)
