package com.flort.evlilik.network.service

import com.flort.evlilik.models.packages.TokenPackage
import com.flort.evlilik.models.packages.request.CouponRequest
import com.flort.evlilik.models.packages.response.CouponResponse
import com.flort.evlilik.models.packages.request.UseCouponRequest
import com.flort.evlilik.models.packages.request.PurchaseTokenRequest
import com.flort.evlilik.models.packages.request.PurchaseVipRequest
import com.flort.evlilik.models.packages.response.PurchaseResponse
import com.flort.evlilik.network.model.ApiResponse
import com.flort.evlilik.network.Routes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PackageService {
    
    @GET(Routes.MAIN_PACKAGES)
    suspend fun getMainPackages(): ApiResponse<List<TokenPackage>>
    
    @GET(Routes.DISCOUNT_PACKAGES)
    suspend fun getDiscountPackages(): ApiResponse<List<TokenPackage>>
    
    @GET(Routes.VIP_PACKAGES)
    suspend fun getVipPackages(): ApiResponse<List<TokenPackage>>
    
    @POST(Routes.APPLY_COUPON)
    suspend fun applyCoupon(@Body request: CouponRequest): ApiResponse<CouponResponse>
    
    @POST(Routes.USE_COUPON)
    suspend fun useCoupon(@Body request: UseCouponRequest): ApiResponse<Any>
    
    @POST(Routes.PURCHASE_TOKEN)
    suspend fun purchaseToken(@Body request: PurchaseTokenRequest): ApiResponse<PurchaseResponse>
    
    @POST(Routes.PURCHASE_VIP)
    suspend fun purchaseVip(@Body request: PurchaseVipRequest): ApiResponse<PurchaseResponse>
}