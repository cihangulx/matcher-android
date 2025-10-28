package com.matcher.matcher.models.ticket

data class RemoveMessage(
    val _id: String? = null,
    val message: String,
    val index: Int,
    val createdAt: String? = null
)
