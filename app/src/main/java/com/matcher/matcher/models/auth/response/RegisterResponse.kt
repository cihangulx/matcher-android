package com.matcher.matcher.models.auth.response

import com.matcher.matcher.models.auth.UserData

data class RegisterResponse(
    val user: UserData,
    val token: String
)
