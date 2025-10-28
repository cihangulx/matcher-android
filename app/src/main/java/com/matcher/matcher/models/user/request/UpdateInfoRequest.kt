package com.matcher.matcher.models.user.request

data class UpdateInfoRequest(
    val name: String? = null,
    val age: Int? = null,
    val gender: Int? = null,
    val city: String? = null,
    val desc: String? = null
)
