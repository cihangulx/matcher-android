package com.flort.evlilik.models.packages.response

import com.flort.evlilik.models.packages.CouponInfo
import com.flort.evlilik.models.packages.TokenPackage

data class CouponResponse(
    val coupon: CouponInfo,
    val packages: List<TokenPackage>
)
