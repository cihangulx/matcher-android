package com.matcher.matcher.models.user.request

import com.matcher.matcher.models.auth.GalleryItem

data class UpdateGalleryRequest(
    val gallery: List<GalleryItem>
)
