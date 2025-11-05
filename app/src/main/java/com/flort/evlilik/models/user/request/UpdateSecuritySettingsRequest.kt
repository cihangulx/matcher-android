package com.flort.evlilik.models.user.request

data class UpdateSecuritySettingsRequest(
    val profileVisible: Boolean? = null,
    val allowNotifications: Boolean? = null,
    val allowMessages: Boolean? = null
)
