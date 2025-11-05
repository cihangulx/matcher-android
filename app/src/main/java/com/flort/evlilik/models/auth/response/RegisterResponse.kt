package com.flort.evlilik.models.auth.response

import com.flort.evlilik.models.auth.UserData

data class RegisterResponse(
    val user: UserData,
    val token: String
)
