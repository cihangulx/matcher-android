package com.flort.evlilik.models.ticket

data class TicketRequest(
    val title: String,      // "request", "subscription", "other"
    val message: String,    // Kullanıcının açıklaması
    val email: String? = null  // Opsiyonel email
)



