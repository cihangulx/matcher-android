package com.matcher.matcher.models.user

import com.matcher.matcher.models.auth.GalleryItem

data class BlockedUser(
    val _id: String,
    val name: String?,
    val age: Int?,
    val gender: Int?,
    val city: String?,
    val desc: String?,
    val gallery: List<GalleryItem>?,
    val like: Int?
)
