package com.matcher.matcher.models.user.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
