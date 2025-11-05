package com.flort.evlilik.models.user
import android.content.Context
import com.flort.evlilik.models.profile.GalleryImage
import com.flort.evlilik.network.ApiClient


class User{
    var _id : String? = null
    var name : String? = null
    var email : String? = null
    var userType : String? = null
    var age : Int? = null
    var gender : Int? = null
    var city : String? = null
    var desc : String? = null
    var gallery : ArrayList<GalleryImage>? = null
    var securitySettings : SecuritySettings? = null
    var wallet : Wallet? = null
    var like : Int? = null
    var isLiked : Boolean? = null

    companion object {
        var current: User? = null
        
        suspend fun updateCurrentUser(context: Context): Boolean {
            return try {
                val authService = ApiClient.getInstance(context).authService
                val response = authService.profile()
                
                if (response.success && response.data != null) {
                    current = response.data
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    fun getPp(): String? {
        // Gallery boşsa null döndür
        if (gallery.isNullOrEmpty()) {
            return null
        }
        
        // Önce isMain olan fotoğrafı ara
        val mainImage = gallery?.find { it.isMain == true }
        if (mainImage != null) {
            return mainImage.url
        }
        
        // isMain olan yoksa ilk sıradaki fotoğrafı döndür
        return gallery?.firstOrNull()?.url
    }
    
    fun getBalance(): Int {
        return wallet?.balance ?: 0
    }
    
    fun isPremium(): Boolean {
        return wallet?.premium ?: false
    }
}
