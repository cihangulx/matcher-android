package com.flort.evlilik.network.service
import com.flort.evlilik.network.Routes
import com.flort.evlilik.models.auth.request.LoginRequest
import com.flort.evlilik.models.auth.response.LoginResponse
import com.flort.evlilik.models.auth.request.RegisterRequest
import com.flort.evlilik.models.auth.response.RegisterResponse
import com.flort.evlilik.models.auth.ForgotPasswordRequest
import com.flort.evlilik.models.auth.CheckEmailRequest
import com.flort.evlilik.models.auth.CheckEmailResponse
import com.flort.evlilik.models.auth.request.GoogleLoginRequest
import com.flort.evlilik.models.auth.response.GoogleLoginResponse
import com.flort.evlilik.models.user.User
import com.flort.evlilik.network.model.ApiResponse
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