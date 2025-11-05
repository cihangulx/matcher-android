package com.flort.evlilik.models.message

data class ApiDeleteResponse(
    val success: Boolean,
    val message: String,
    val data: DeleteMessageData? = null
)
