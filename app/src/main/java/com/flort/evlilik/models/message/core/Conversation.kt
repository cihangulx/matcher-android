package com.flort.evlilik.models.message.core

import com.google.gson.annotations.SerializedName
import com.flort.evlilik.models.message.ConversationUser

data class Conversation(
    @SerializedName("_id") val id: String,
    val participants: List<ConversationUser>? = null,
    val otherUser: ConversationUser? = null,
    val lastMessage: Message? = null,
    val lastMessageAt: String = "",
    val unreadCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String? = null
) {
    fun getOtherUser(currentUserId: String): ConversationUser? {
        if (otherUser != null) {
            return otherUser
        }
        return participants?.find { it.id != currentUserId }
    }
}
