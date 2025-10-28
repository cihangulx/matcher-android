package com.matcher.matcher.models.auth

data class CheckEmailResponse(
    val email: String,
    val exists: Boolean
)
