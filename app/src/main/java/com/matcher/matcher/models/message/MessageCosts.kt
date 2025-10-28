package com.matcher.matcher.models.message

data class MessageCosts(
    val text: Int,
    val image: Int,
    val video: Int,
    val audio: Int,
    val file: Int
)
