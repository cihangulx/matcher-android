package com.matcher.matcher.models.message.response

import com.matcher.matcher.models.message.WalletData

data class WalletResponse(
    val success: Boolean,
    val data: WalletData
)
