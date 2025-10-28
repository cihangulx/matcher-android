package com.matcher.matcher.models.auth.request

import com.matcher.matcher.models.auth.GalleryItem

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val age: Int? = null,
    val gender: Int? = null,
    val gallery: List<GalleryItem>? = null
)
