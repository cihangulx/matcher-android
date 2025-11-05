package com.flort.evlilik.models.packages.response

data class PurchaseResponse(
    val paymentId: String,
    val transactionId: String,
    val tokenAmount: Int? = null,
    val vipDuration: Int? = null,
    val newBalance: Int? = null,
    val premiumExpiry: String? = null,
    val price: Double,
    val originalPrice: Double,
    val discount: Double
)
