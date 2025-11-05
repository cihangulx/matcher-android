package com.flort.evlilik.models.packages

data class TokenPackage(
    val _id: String? = null,
    val name: String? = null,
    val desc: String? = null,
    val active: Boolean? = null,
    val currentPrice: Double? = null,
    val oldPrice: Double? = null,
    val sku: String? = null,
    val tokenAmount: Int? = null, // main, discount, coupon için: jeton miktarı | vip için: gün sayısı
    val type: String? = null, // main, discount, coupon, vip
    val createdAt: String? = null
)
