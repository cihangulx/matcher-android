package com.flort.evlilik.network.service

import com.flort.evlilik.network.Routes
import com.flort.evlilik.models.auth.AppOptions
import com.flort.evlilik.network.model.ApiResponse
import retrofit2.http.GET

interface SettingsService {
    @GET(Routes.Companion.SETTINGS)
    suspend fun getSettings(): ApiResponse<AppOptions>
}