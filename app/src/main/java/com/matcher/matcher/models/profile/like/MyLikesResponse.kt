package com.matcher.matcher.models.profile.like

data class MyLikesResponse(
    val isVisible: Boolean,
    val isPremium: Boolean,
    val totalLikes: Int,
    val users: List<LikedUser>,
    val message: String? = null
)
