package com.flort.evlilik.models.message.response

import com.flort.evlilik.models.message.WalletData

data class WalletResponse(
    val success: Boolean,
    val data: WalletData
)
