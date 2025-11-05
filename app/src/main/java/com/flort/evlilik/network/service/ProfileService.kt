package com.flort.evlilik.network.service

import com.flort.evlilik.network.model.ApiResponse
import com.flort.evlilik.network.Routes
import com.flort.evlilik.models.user.User
import com.flort.evlilik.models.profile.like.LikeRequest
import com.flort.evlilik.models.profile.like.LikeResponse
import com.flort.evlilik.models.profile.like.LikeStatusResponse
import com.flort.evlilik.models.profile.like.MyLikesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ProfileService {
    @GET(Routes.HOME_PROFILES)
    suspend fun getHomeProfiles(): ApiResponse<List<User>>

    @POST(Routes.LIKE_PROFILE)
    suspend fun likeProfile(@Body request: LikeRequest): ApiResponse<LikeResponse>

    @POST(Routes.UNLIKE_PROFILE)
    suspend fun unlikeProfile(@Body request: LikeRequest): ApiResponse<LikeResponse>

    @GET(Routes.CHECK_LIKE_STATUS)
    suspend fun checkLikeStatus(@Path("targetUserId") targetUserId: String): ApiResponse<LikeStatusResponse>

    @GET(Routes.MY_LIKES)
    suspend fun getMyLikes(): ApiResponse<MyLikesResponse>
}