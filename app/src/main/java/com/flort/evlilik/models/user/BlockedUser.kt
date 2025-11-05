package com.flort.evlilik.models.user

import com.flort.evlilik.models.auth.GalleryItem

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
