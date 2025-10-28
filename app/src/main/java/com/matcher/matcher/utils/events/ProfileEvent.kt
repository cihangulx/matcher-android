package com.matcher.matcher.utils.events

/**
 * Profil sayfasında gerçekleşen olayları temsil eden sealed class
 */
sealed class ProfileEvent {
    /**
     * Profil beğenildiğinde veya beğeni kaldırıldığında
     */
    data class ProfileLikeChanged(
        val userId: String, 
        val isLiked: Boolean,
        val likeCount: Int? = null
    ) : ProfileEvent()
    
    /**
     * Profil engellendiğinde veya engel kaldırıldığında
     */
    data class ProfileBlockChanged(val userId: String, val isBlocked: Boolean) : ProfileEvent()
    
    /**
     * Profil rapor edildiğinde
     */
    data class ProfileReported(val userId: String) : ProfileEvent()
    
    /**
     * Herhangi bir profil değişikliği olduğunda - genel refresh için
     */
    object RefreshRequired : ProfileEvent()
}

