package com.matcher.matcher.models.packages.response

import com.matcher.matcher.models.packages.CouponInfo
import com.matcher.matcher.models.packages.TokenPackage

data class CouponResponse(
    val coupon: CouponInfo,
    val packages: List<TokenPackage>
)
