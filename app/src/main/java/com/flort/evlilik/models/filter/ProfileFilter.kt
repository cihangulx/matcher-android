package com.flort.evlilik.models.filter

/**
 * Profil filtreleme parametreleri
 */
data class ProfileFilter(
    val minAge: Int? = null,
    val maxAge: Int? = null, // null = sınırsız (50+ gibi)
    val gender: Int? = null // 0: bilinmiyor, 1: erkek, 2: kadın
) {
    companion object {
        const val MIN_AGE = 18
        const val MAX_AGE = 50 // 50 seçilirse 50+ anlamına gelir
        
        // Varsayılan filtre (tüm profiller)
        val DEFAULT = ProfileFilter(
            minAge = MIN_AGE,
            maxAge = null, // Sınırsız
            gender = null
        )
    }
    
    /**
     * Filtrenin aktif olup olmadığını kontrol eder
     */
    fun isActive(): Boolean {
        return minAge != DEFAULT.minAge || 
               maxAge != null || 
               gender != null
    }
}

