package com.matcher.matcher.network.service

import com.matcher.matcher.network.Routes
import com.matcher.matcher.models.auth.AppOptions
import com.matcher.matcher.network.model.ApiResponse
import retrofit2.http.GET

interface SettingsService {
    @GET(Routes.Companion.SETTINGS)
    suspend fun getSettings(): ApiResponse<AppOptions>
}