package com.matcher.matcher.models.ticket.response

data class ReportData(
    val id: String,
    val reportedUserId: String,
    val reason: String,
    val status: String,
    val createdAt: String
)
