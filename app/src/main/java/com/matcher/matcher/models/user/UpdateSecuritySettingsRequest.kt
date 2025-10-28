package com.matcher.matcher.models.user

data class UpdateSecuritySettingsRequest(
    val profileVisible: Boolean? = null,
    val allowNotifications: Boolean? = null,
    val allowMessages: Boolean? = null
)
