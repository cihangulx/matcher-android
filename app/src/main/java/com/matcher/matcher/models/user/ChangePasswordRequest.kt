package com.matcher.matcher.models.user

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
