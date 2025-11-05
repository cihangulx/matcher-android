package com.flort.evlilik.models.auth

data class CheckEmailResponse(
    val email: String,
    val exists: Boolean
)
