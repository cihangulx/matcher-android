package com.matcher.matcher.utils.helpers

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.onesignal.notifications.INotificationClickListener
import com.onesignal.notifications.INotificationLifecycleListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * OneSignal push notification yönetimi için helper sınıfı
 * OneSignal 5.1.37 sürümü için güncellenmiş
 */
object OneSignalHelper {
    
    private const val TAG = "OneSignalHelper"
    
    /**
     * OneSignal'i başlatır
     * @param context Uygulama context'i
     * @param appId OneSignal App ID
     */
    fun initialize(context: Context, appId: String) {
        try {
            // OneSignal log seviyesini ayarla
            OneSignal.Debug.logLevel = LogLevel.VERBOSE
            
            // OneSignal'i başlat
            OneSignal.initWithContext(context, appId)

            // Notification permission isteği (suspend function olduğu için coroutine içinde)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    OneSignal.Notifications.requestPermission(true)
                    Log.d(TAG, "OneSignal başarıyla başlatıldı")
                } catch (e: Exception) {
                    Log.e(TAG, "OneSignal permission hatası: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal başlatma hatası: ${e.message}")
        }
    }
    
    /**
     * Kullanıcı ID'sini OneSignal'e gönderir
     * @param userId Kullanıcı ID'si
     */
    fun setUserId(userId: String) {
        try {
            OneSignal.login(userId)
            Log.d(TAG, "OneSignal kullanıcı ID'si ayarlandı: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal kullanıcı ID ayarlama hatası: ${e.message}")
        }
    }
    
    /**
     * Kullanıcıyı OneSignal'den çıkarır
     */
    fun logout() {
        try {
            OneSignal.logout()
            Log.d(TAG, "OneSignal kullanıcı çıkışı yapıldı")
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal çıkış hatası: ${e.message}")
        }
    }
    
    /**
     * Kullanıcı etiketlerini ayarlar
     * @param tags Kullanıcı etiketleri (key-value pairs)
     */
    fun setUserTags(tags: Map<String, String>) {
        try {
            OneSignal.User.addTags(tags)
            Log.d(TAG, "OneSignal kullanıcı etiketleri ayarlandı: $tags")
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal etiket ayarlama hatası: ${e.message}")
        }
    }
    
    /**
     * Belirli bir etiketi siler
     * @param key Silinecek etiket anahtarı
     */
    fun removeUserTag(key: String) {
        try {
            OneSignal.User.removeTag(key)
            Log.d(TAG, "OneSignal etiket silindi: $key")
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal etiket silme hatası: ${e.message}")
        }
    }
    
    /**
     * Notification permission durumunu kontrol eder
     * @return true eğer permission verilmişse
     */
    fun isNotificationPermissionGranted(): Boolean {
        return try {
            OneSignal.Notifications.permission
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal permission kontrol hatası: ${e.message}")
            false
        }
    }
    
    /**
     * Notification permission ister
     */
    fun requestNotificationPermission() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                OneSignal.Notifications.requestPermission(true)
                Log.d(TAG, "OneSignal notification permission istendi")
            } catch (e: Exception) {
                Log.e(TAG, "OneSignal permission isteme hatası: ${e.message}")
            }
        }
    }
    
    /**
     * OneSignal player ID'sini alır
     * @return Player ID veya null
     */
    fun getPlayerId(): String? {
        return try {
            OneSignal.User.onesignalId
        } catch (e: Exception) {
            Log.e(TAG, "OneSignal player ID alma hatası: ${e.message}")
            null
        }
    }
}