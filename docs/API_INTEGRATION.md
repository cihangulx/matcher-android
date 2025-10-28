# API Entegrasyonu

Matcher Android uygulamasının backend API ile entegrasyonu ve ağ katmanı yapısı.

> **📱 Toast Sistemi**: API hatalarını kullanıcıya bildirmek için [Toast Sistemi](../TOAST_USAGE_EXAMPLES.md) sayfasına bakın.

## 🌐 API Yapısı

### Base URL
```
https://admin54.askologapp.com:3000/api/
```

### Socket URL
```
https://admin54.askologapp.com:3001
```

## 🔧 ApiClient Yapısı

### Singleton Pattern
```kotlin
@SuppressLint("StaticFieldLeak")
class ApiClient private constructor(private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ApiClient? = null
        
        fun getInstance(context: Context): ApiClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiClient(context).also { INSTANCE = it }
            }
        }
    }
}
```

### Retrofit Konfigürasyonu
```kotlin
private val retrofit: Retrofit by lazy {
    Retrofit.Builder()
        .baseUrl(Routes.Companion.BASE_URL)
        .client(createOkHttpClient())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```

## 📡 API Servisleri

### 1. AuthService - Kimlik Doğrulama
```kotlin
interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    
    @POST("auth/google-login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): GoogleLoginResponse
    
    @GET("auth/profile")
    suspend fun profile(): ApiResponse<User>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse<Any>
    
    @POST("auth/check-email")
    suspend fun checkEmail(@Body request: CheckEmailRequest): CheckEmailResponse
}
```

### 2. UserService - Kullanıcı Yönetimi
```kotlin
interface UserService {
    @POST("users/update-info")
    suspend fun updateInfo(@Body request: UpdateInfoRequest): ApiResponse<User>
    
    @POST("users/update-password")
    suspend fun updatePassword(@Body request: ChangePasswordRequest): ApiResponse<Any>
    
    @POST("users/update-security-settings")
    suspend fun updateSecuritySettings(@Body request: UpdateSecuritySettingsRequest): SecuritySettingsResponse
    
    @GET("users/gallery")
    suspend fun getGallery(): ApiResponse<List<GalleryImage>>
    
    @POST("users/update-gallery")
    suspend fun updateGallery(@Body request: UpdateGalleryRequest): ApiResponse<Any>
    
    @POST("users/block")
    suspend fun blockUser(@Body request: BlockUserRequest): ApiResponse<Any>
    
    @POST("users/unblock")
    suspend fun unblockUser(@Body request: UnblockUserRequest): ApiResponse<Any>
    
    @GET("users/blocked-users")
    suspend fun getBlockedUsers(): ApiResponse<List<BlockedUser>>
    
    @POST("users/toggle-premium")
    suspend fun togglePremium(): ApiResponse<Any>
}
```

### 3. ProfileService - Profil Yönetimi
```kotlin
interface ProfileService {
    @GET("profiles/home")
    suspend fun getHomeProfiles(@Query("page") page: Int = 1): ApiResponse<List<Profile>>
    
    @POST("profiles/like")
    suspend fun likeProfile(@Body request: LikeRequest): LikeResponse
    
    @POST("profiles/unlike")
    suspend fun unlikeProfile(@Body request: LikeRequest): LikeResponse
    
    @GET("profiles/like-status/{targetUserId}")
    suspend fun checkLikeStatus(@Path("targetUserId") targetUserId: String): LikeStatusResponse
    
    @GET("profiles/my-likes")
    suspend fun getMyLikes(): MyLikesResponse
}
```

### 4. MessageService - Mesajlaşma
```kotlin
interface MessageService {
    @GET("messages/conversations")
    suspend fun getConversations(): ConversationsResponse
    
    @GET("messages/conversations/{conversationId}/messages")
    suspend fun getMessages(@Path("conversationId") conversationId: String): MessagesResponse
    
    @POST("messages/conversations/start")
    suspend fun startConversation(@Body request: StartConversationRequest): ConversationResponse
    
    @DELETE("messages/conversations/{conversationId}")
    suspend fun deleteConversation(@Path("conversationId") conversationId: String): ApiDeleteResponse
    
    @DELETE("messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): ApiDeleteResponse
    
    @GET("messages/unread")
    suspend fun getUnreadCount(): UnreadCountResponse
    
    @GET("options/message-costs")
    suspend fun getMessageCosts(): MessageCostsResponse
    
    @GET("users/wallet")
    suspend fun getWallet(): WalletResponse
    
    @GET("users/check-message-permission")
    suspend fun checkMessagePermission(): MessagePermissionResponse
}
```

### 5. PackageService - Paket Yönetimi
```kotlin
interface PackageService {
    @GET("packages/main")
    suspend fun getMainPackages(): ApiResponse<List<TokenPackage>>
    
    @GET("packages/discount")
    suspend fun getDiscountPackages(): ApiResponse<List<TokenPackage>>
    
    @GET("packages/vip")
    suspend fun getVipPackages(): ApiResponse<List<TokenPackage>>
    
    @POST("packages/apply-coupon")
    suspend fun applyCoupon(@Body request: CouponRequest): CouponResponse
    
    @POST("packages/use-coupon")
    suspend fun useCoupon(@Body request: UseCouponRequest): ApiResponse<Any>
}
```

### 6. GiftService - Hediye Sistemi
```kotlin
interface GiftService {
    @GET("gifts")
    suspend fun getGifts(): ApiResponse<List<Gift>>
}
```

### 7. FileService - Dosya Yönetimi
```kotlin
interface FileService {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): FileUploadResponse
}
```

### 8. TicketService - Rapor Sistemi
```kotlin
interface TicketService {
    @POST("tickets/")
    suspend fun sendTicket(@Body request: SendTicketRequest): ApiResponse<Any>
    
    @POST("tickets/remove-messages")
    suspend fun removeMessages(@Body request: RemoveMessageRequest): ApiResponse<Any>
    
    @POST("tickets/start-messages")
    suspend fun startMessages(@Body request: StartMessageRequest): ApiResponse<Any>
    
    @GET("tickets/report-reasons")
    suspend fun getReportReasons(): ApiResponse<List<ReportReason>>
    
    @POST("tickets/report")
    suspend fun sendReport(@Body request: ReportRequest): ReportResponse
}
```

## 🔐 Authentication

### Token Yönetimi
```kotlin
private fun createOkHttpClient(): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${getToken()}")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}

private suspend fun getToken(): String {
    return preferencesManager.getToken().first() ?: ""
}
```

## 📡 Socket.IO Entegrasyonu

### SocketManager
```kotlin
class SocketManager {
    private var socket: Socket? = null
    
    fun connect(token: String) {
        socket = IO.socket("$SOCKET_URL?token=$token")
        socket?.connect()
    }
    
    fun disconnect() {
        socket?.disconnect()
        socket = null
    }
    
    // Mesaj gönderme
    fun sendMessage(message: Message) {
        socket?.emit("send_message", message.toJson())
    }
    
    // Yeni mesaj dinleme
    fun onNewMessage(callback: (SocketMessageEvent) -> Unit) {
        socket?.on("new_message") { args ->
            val messageEvent = SocketMessageEvent.fromJson(args[0].toString())
            callback(messageEvent)
        }
    }
    
    // Kullanıcı durumu dinleme
    fun onUserStatus(callback: (UserStatus) -> Unit) {
        socket?.on("user_status") { args ->
            val userStatus = UserStatus.fromJson(args[0].toString())
            callback(userStatus)
        }
    }
    
    // Mesaj durumu güncelleme
    fun onMessageStatusUpdate(callback: (MessageStatusUpdate) -> Unit) {
        socket?.on("message_status_update") { args ->
            val statusUpdate = MessageStatusUpdate.fromJson(args[0].toString())
            callback(statusUpdate)
        }
    }
}
```

## 🔄 Veri Modelleri

### API Response Wrapper
```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
```

### User Model
```kotlin
class User {
    var _id: String? = null
    var name: String? = null
    var email: String? = null
    var userType: String? = null
    var age: Int? = null
    var gender: Int? = null
    var city: String? = null
    var desc: String? = null
    var gallery: ArrayList<GalleryImage>? = null
    var securitySettings: SecuritySettings? = null
    var wallet: Wallet? = null
    var like: Int? = null
    var isLiked: Boolean? = null
    
    companion object {
        var current: User? = null
        
        suspend fun updateCurrentUser(context: Context): Boolean {
            return try {
                val authService = ApiClient.getInstance(context).authService
                val response = authService.profile()
                
                if (response.success && response.data != null) {
                    current = response.data
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
```

## 🛠️ Repository Pattern

### GalleryRepository
```kotlin
class GalleryRepository(private val context: Context) {
    private val apiClient = ApiClient.getInstance(context)
    
    suspend fun getGallery(): Result<List<GalleryImage>> {
        return try {
            val response = apiClient.userService.getGallery()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGallery(gallery: List<GalleryImage>): Result<Boolean> {
        return try {
            val request = UpdateGalleryRequest(gallery)
            val response = apiClient.userService.updateGallery(request)
            Result.success(response.success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🔧 Error Handling

### ApiException
```kotlin
class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)
```

### Error Handling Örneği
```kotlin
suspend fun login(email: String, password: String): Result<User> {
    return try {
        val request = LoginRequest(email, password)
        val response = apiClient.authService.login(request)
        
        if (response.success && response.data != null) {
            // Token'ı kaydet
            preferencesManager.saveToken(response.data.token)
            Result.success(response.data.user)
        } else {
            Result.failure(ApiException(400, response.message ?: "Login failed"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## 📱 Network State Management

### Connectivity Check
```kotlin
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = connectivityManager.activeNetworkInfo
    return networkInfo?.isConnected == true
}
```

### Retry Mechanism
```kotlin
suspend fun <T> retryApiCall(
    maxRetries: Int = 3,
    delay: Long = 1000,
    apiCall: suspend () -> T
): T {
    repeat(maxRetries - 1) { attempt ->
        try {
            return apiCall()
        } catch (e: Exception) {
            if (attempt == maxRetries - 2) throw e
            delay(delay * (attempt + 1))
        }
    }
    return apiCall()
}
```

## 🔒 Security

### SSL Pinning (Opsiyonel)
```kotlin
private fun createOkHttpClient(): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add("admin54.askologapp.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
    
    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}
```

### Request/Response Logging
```kotlin
val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
```

Bu API entegrasyonu yapısı, uygulamanın backend ile güvenli ve verimli bir şekilde iletişim kurmasını sağlar.
