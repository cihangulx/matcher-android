package com.matcher.matcher.models.profile

class Profile {
    var userId: String? = null
    var gallery: ArrayList<GalleryImage> = ArrayList()
    var name: String? = null
    var age: Int? = null
    var statusDesc: String? = null
    var like: Int? = null
    var isLiked: Boolean? = null
    
    /**
     * Profil fotoğrafı URL'ini döndürür
     * Öncelik sırası: pp -> gallery'deki isMain -> gallery'deki ilk fotoğraf
     */
    fun getPp(): String? {
        // Gallery boşsa null döndür
        if (gallery.isEmpty()) {
            return null
        }
        
        // isMain olan fotoğrafı ara
        val mainImage = gallery.find { it.isMain == true }
        if (mainImage != null && !mainImage.url.isNullOrEmpty()) {
            return mainImage.url
        }
        
        // isMain olan yoksa ilk fotoğrafı döndür
        return gallery.firstOrNull()?.url
    }
}
class GalleryImage {
    var index: Int? = 0
    var url: String? = null
    var isMain: Boolean? = false
}