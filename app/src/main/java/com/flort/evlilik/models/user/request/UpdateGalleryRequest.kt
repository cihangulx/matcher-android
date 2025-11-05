package com.flort.evlilik.models.user.request

import com.flort.evlilik.models.auth.GalleryItem

data class UpdateGalleryRequest(
    val gallery: List<GalleryItem>
)
