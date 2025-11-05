package com.flort.evlilik.models.ticket.request

data class ReportRequest(
    val reportedUserId: String,
    val reason: String
)
