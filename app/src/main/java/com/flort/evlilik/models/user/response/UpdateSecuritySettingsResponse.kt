package com.flort.evlilik.models.user.response

import com.flort.evlilik.models.user.SecuritySettings

data class UpdateSecuritySettingsResponse(
    val securitySettings: SecuritySettings
)
