package com.matcher.matcher.models.packages.request

data class PurchaseTokenRequest(
    val sku: String,
    val paymentMethod: String = "google",
    val paymentData: Map<String, Any> = emptyMap(),
    val couponCode: String? = null
)
