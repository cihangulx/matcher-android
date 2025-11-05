package com.flort.evlilik.models.user

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
