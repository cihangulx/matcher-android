package com.flort.evlilik.models.user

data class SecuritySettings(
    val profileVisible: Boolean,
    val allowNotifications: Boolean,
    val allowMessages: Boolean
)
