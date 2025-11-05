package com.flort.evlilik.models.auth.request

import com.flort.evlilik.models.auth.GalleryItem

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String? = null,
    val age: Int? = null,
    val gender: Int? = null,
    val gallery: List<GalleryItem>? = null
)
