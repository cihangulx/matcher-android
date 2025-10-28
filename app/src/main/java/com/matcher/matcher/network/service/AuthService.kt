package com.matcher.matcher.network.service
import com.matcher.matcher.network.Routes
import com.matcher.matcher.models.auth.request.LoginRequest
import com.matcher.matcher.models.auth.response.LoginResponse
import com.matcher.matcher.models.auth.request.RegisterRequest
import com.matcher.matcher.models.auth.response.RegisterResponse
import com.matcher.matcher.models.auth.ForgotPasswordRequest
import com.matcher.matcher.models.auth.CheckEmailRequest
import com.matcher.matcher.models.auth.CheckEmailResponse
import com.matcher.matcher.models.auth.request.GoogleLoginRequest
import com.matcher.matcher.models.auth.response.GoogleLoginResponse
import com.matcher.matcher.models.user.User
import com.matcher.matcher.network.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST(Routes.Companion.LOGIN)
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST(Routes.Companion.REGISTER)
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterResponse>

    @GET(Routes.Companion.PROFILE)
    suspend fun profile(): ApiResponse<User>

    @GET(Routes.VALIDATE_TOKEN)
    suspend fun validateToken(): ApiResponse<User>

    @POST(Routes.Companion.LOGOUT)
    suspend fun logout(): ApiResponse<Unit>

    @POST(Routes.Companion.LOGOUT_ALL)
    suspend fun logoutAll(): ApiResponse<Unit>

    @POST(Routes.FORGOT_PASSWORD)
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse<Unit>

    @POST(Routes.CHECK_EMAIL)
    suspend fun checkEmail(@Body request: CheckEmailRequest): ApiResponse<CheckEmailResponse>

    @POST(Routes.GOOGLE_LOGIN)
    suspend fun googleLogin(@Body request: GoogleLoginRequest): ApiResponse<GoogleLoginResponse>

}