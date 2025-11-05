package com.flort.evlilik.models.user.response

import com.flort.evlilik.models.auth.GalleryItem

data class UpdateGalleryResponse(
    val gallery: List<GalleryItem>
)
