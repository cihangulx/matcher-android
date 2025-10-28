package com.matcher.matcher.models.auth.request

data class GoogleLoginRequest(
    val token: String,
    val email: String,
    val name: String,
    val age: Int? = null
)
