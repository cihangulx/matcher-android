package com.matcher.matcher.models.message.core

import com.google.gson.annotations.SerializedName

data class ReplyMessage(
    @SerializedName("_id") val id: String,
    val senderId: String,
    val senderName: String? = null,
    val content: String,
    val type: String = "text",
    val mediaUrl: String? = null
)
