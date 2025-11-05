package com.flort.evlilik.network.model

/**
 * Generic API response wrapper
 * Post ve put isteklerinde kullanılır.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
) 