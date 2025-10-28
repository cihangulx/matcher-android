package com.matcher.matcher.network.service

import com.matcher.matcher.network.Routes
import com.matcher.matcher.models.user.request.UpdateInfoRequest
import com.matcher.matcher.models.user.request.ChangePasswordRequest
import com.matcher.matcher.models.user.request.UpdateSecuritySettingsRequest
import com.matcher.matcher.models.user.request.UpdateGalleryRequest
import com.matcher.matcher.models.user.response.GalleryResponse
import com.matcher.matcher.models.user.response.UpdateGalleryResponse
import com.matcher.matcher.models.user.response.UpdateInfoResponse
import com.matcher.matcher.models.user.response.UpdateSecuritySettingsResponse
import com.matcher.matcher.models.user.request.BlockRequest
import com.matcher.matcher.models.user.response.BlockResponse
import com.matcher.matcher.models.user.response.BlockStatusResponse
import com.matcher.matcher.models.user.BlockedUser
import com.matcher.matcher.models.user.response.TogglePremiumResponse
import com.matcher.matcher.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserService {

    @GET(Routes.GET_GALLERY)
    suspend fun getGallery(): ApiResponse<GalleryResponse>

    @PUT(Routes.UPDATE_GALLERY)
    suspend fun updateGallery(@Body request: UpdateGalleryRequest): ApiResponse<UpdateGalleryResponse>

    @PUT(Routes.UPDATE_INFO)
    suspend fun updateInfo(@Body request: UpdateInfoRequest): ApiResponse<UpdateInfoResponse>

    @PUT(Routes.UPDATE_PASSWORD)
    suspend fun updatePassword(@Body request: ChangePasswordRequest): ApiResponse<Unit>

    @PUT(Routes.UPDATE_SECURITY_SETTINGS)
    suspend fun updateSecuritySettings(@Body request: UpdateSecuritySettingsRequest): ApiResponse<UpdateSecuritySettingsResponse>

    @POST(Routes.BLOCK_USER)
    suspend fun blockUser(@Body request: BlockRequest): ApiResponse<BlockResponse>

    @POST(Routes.UNBLOCK_USER)
    suspend fun unblockUser(@Body request: BlockRequest): ApiResponse<BlockResponse>

    @GET(Routes.CHECK_BLOCK_STATUS)
    suspend fun checkBlockStatus(@Path("targetUserId") targetUserId: String): ApiResponse<BlockStatusResponse>

    @GET(Routes.BLOCKED_USERS)
    suspend fun getBlockedUsers(): ApiResponse<List<BlockedUser>>
    
    @POST(Routes.TOGGLE_PREMIUM)
    suspend fun togglePremium(): ApiResponse<TogglePremiumResponse>
}