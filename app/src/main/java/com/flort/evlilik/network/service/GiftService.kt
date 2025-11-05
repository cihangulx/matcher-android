package com.flort.evlilik.network.service

import com.flort.evlilik.models.gift.Gift
import com.flort.evlilik.network.Routes
import com.flort.evlilik.network.model.ApiResponse
import retrofit2.http.GET

interface GiftService {
    @GET(Routes.GET_GIFTS)
    suspend fun getGifts(): ApiResponse<List<Gift>>
}
