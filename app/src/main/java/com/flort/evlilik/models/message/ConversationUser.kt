package com.flort.evlilik.models.message

import com.google.gson.annotations.SerializedName

data class ConversationUser(
    @SerializedName("_id") val id: String = "",
    val name: String? = null,
    val gallery: List<Gallery>? = null,
    val lastSeen: String? = null,
    val isOnline: Boolean? = null
) {
    fun getPhotoUrl(): String? {
        return gallery?.find { it.isMain }?.url ?: gallery?.firstOrNull()?.url
    }
}
