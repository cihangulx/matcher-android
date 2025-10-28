package com.matcher.matcher.models.message

data class Gallery(
    val url: String,
    val isMain: Boolean = false,
    val index: Int = 0
)
