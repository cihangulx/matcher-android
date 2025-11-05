package com.flort.evlilik.models.auth.response

import com.flort.evlilik.models.auth.UserData

data class LoginResponse(
    val user: UserData,
    val token: String
)
