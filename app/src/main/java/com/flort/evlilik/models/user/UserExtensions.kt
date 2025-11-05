package com.flort.evlilik.models.user

/**
 * User objesini kopyalar (immutable yaklaşım için)
 * Sadece belirtilen property'ler değiştirilir, diğerleri aynı kalır
 */
fun User.copy(
    _id: String? = this._id,
    name: String? = this.name,
    email: String? = this.email,
    userType: String? = this.userType,
    age: Int? = this.age,
    gender: Int? = this.gender,
    city: String? = this.city,
    desc: String? = this.desc,
    gallery: ArrayList<com.flort.evlilik.models.profile.GalleryImage>? = this.gallery,
    securitySettings: SecuritySettings? = this.securitySettings,
    wallet: Wallet? = this.wallet,
    like: Int? = this.like,
    isLiked: Boolean? = this.isLiked
): User {
    return User().apply {
        this._id = _id
        this.name = name
        this.email = email
        this.userType = userType
        this.age = age
        this.gender = gender
        this.city = city
        this.desc = desc
        this.gallery = gallery
        this.securitySettings = securitySettings
        this.wallet = wallet
        this.like = like
        this.isLiked = isLiked
    }
}

