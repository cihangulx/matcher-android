package com.flort.evlilik.models.profile.like

data class LikedUser(
    val _id: String,
    val name: String?,
    val age: Int?,
    val gender: Int?,
    val city: String?,
    val desc: String?,
    val gallery: ArrayList<com.flort.evlilik.models.profile.GalleryImage>?,
    val like: Int?,
    val isLikedBack: Boolean?
)
