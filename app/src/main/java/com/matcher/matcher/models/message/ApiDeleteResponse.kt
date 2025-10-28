package com.matcher.matcher.models.message

data class ApiDeleteResponse(
    val success: Boolean,
    val message: String,
    val data: DeleteMessageData? = null
)
