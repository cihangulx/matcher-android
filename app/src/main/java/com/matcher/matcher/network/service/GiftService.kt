package com.matcher.matcher.network.service

import com.matcher.matcher.models.gift.Gift
import com.matcher.matcher.network.Routes
import com.matcher.matcher.network.model.ApiResponse
import retrofit2.http.GET

interface GiftService {
    @GET(Routes.GET_GIFTS)
    suspend fun getGifts(): ApiResponse<List<Gift>>
}
