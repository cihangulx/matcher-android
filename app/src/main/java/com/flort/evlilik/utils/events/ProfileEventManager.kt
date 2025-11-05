package com.flort.evlilik.utils.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Profil olaylarını yönetmek için singleton event manager
 * 
 * Kullanım:
 * - Event göndermek için: ProfileEventManager.emitEvent(event)
 * - Event dinlemek için: ProfileEventManager.events.collect { event -> ... }
 */
object ProfileEventManager {
    private val _events = MutableSharedFlow<ProfileEvent>(
        replay = 0, // Yeni subscriber'lar önceki event'leri almaz
        extraBufferCapacity = 10 // Buffer kapasitesi
    )
    
    /**
     * Event akışı - collect ederek dinleyebilirsiniz
     */
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    
    /**
     * Event gönderir
     * 
     * @param event Gönderilecek event
     */
    suspend fun emitEvent(event: ProfileEvent) {
        _events.emit(event)
    }
    
    /**
     * Hızlı erişim: Profil beğeni durumu değiştiğinde event gönder
     */
    suspend fun notifyProfileLikeChanged(userId: String, isLiked: Boolean, likeCount: Int? = null) {
        emitEvent(ProfileEvent.ProfileLikeChanged(userId, isLiked, likeCount))
    }
    
    /**
     * Hızlı erişim: Profil engel durumu değiştiğinde event gönder
     */
    suspend fun notifyProfileBlockChanged(userId: String, isBlocked: Boolean) {
        emitEvent(ProfileEvent.ProfileBlockChanged(userId, isBlocked))
    }
    
    /**
     * Hızlı erişim: Profil rapor edildiğinde event gönder
     */
    suspend fun notifyProfileReported(userId: String) {
        emitEvent(ProfileEvent.ProfileReported(userId))
    }
    
    /**
     * Hızlı erişim: Genel refresh gerektiğinde event gönder
     */
    suspend fun notifyRefreshRequired() {
        emitEvent(ProfileEvent.RefreshRequired)
    }
}

