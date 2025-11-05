package com.flort.evlilik.models.user.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
