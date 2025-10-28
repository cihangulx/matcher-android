package com.matcher.matcher.models.user

data class UpdateInfoRequest(
    val name: String,
    val age: Int? = null,
    val gender: Int? = null,  // 0: bilinmiyor, 1: erkek, 2: kadın
    val desc: String? = null  // Hakkımda bilgisi
)
