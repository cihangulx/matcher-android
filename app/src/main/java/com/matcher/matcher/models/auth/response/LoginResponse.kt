package com.matcher.matcher.models.auth.response

import com.matcher.matcher.models.auth.UserData

data class LoginResponse(
    val user: UserData,
    val token: String
)
