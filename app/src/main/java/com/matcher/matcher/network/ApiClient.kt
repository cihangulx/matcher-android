package com.matcher.matcher.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.matcher.matcher.network.service.AuthService
import com.matcher.matcher.network.service.TicketService
import com.matcher.matcher.network.service.SettingsService
import com.matcher.matcher.network.service.UserService
import com.matcher.matcher.network.service.FileService
import com.matcher.matcher.network.service.PackageService
import com.matcher.matcher.network.service.ProfileService
import com.matcher.matcher.network.service.MessageService
import com.matcher.matcher.network.service.GiftService
import com.matcher.matcher.utils.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
@SuppressLint("StaticFieldLeak")
class ApiClient private constructor(private val context: Context) {
    private val preferencesManager = PreferencesManager.getInstance(context)
    private val TAG = "ApiClient"

    // Retrofit ve ApiService'i başlat
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Routes.Companion.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val ticketService: TicketService by lazy {
        retrofit.create(TicketService::class.java)
    }

    val settingsService: SettingsService by lazy {
        retrofit.create(SettingsService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val fileService: FileService by lazy {
        retrofit.create(FileService::class.java)
    }

    val packageService: PackageService by lazy {
        retrofit.create(PackageService::class.java)
    }

    val profileService: ProfileService by lazy {
        retrofit.create(ProfileService::class.java)
    }

    val messageService: MessageService by lazy {
        retrofit.create(MessageService::class.java)
    }

    val giftService: GiftService by lazy {
        retrofit.create(GiftService::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        Log.d(TAG, "ApiClient başlatılıyor...")

        // Logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        Log.d(TAG, "Logging interceptor oluşturuldu")

        // Auth interceptor
        val authInterceptor = Interceptor { chain ->
            Log.d(TAG, "Auth interceptor çalışıyor...")
            val originalRequest = chain.request()
            Log.d(TAG, "Orijinal istek: ${originalRequest.method} ${originalRequest.url}")
            Log.d(TAG, "Orijinal headers: ${originalRequest.headers}")
            // Token'ı header'a ekle
            val token = runBlocking { preferencesManager.authToken.first() }
            val newRequest = originalRequest.newBuilder().apply {
                token?.let { t ->
                    header("Authorization", "Bearer $t")
                } ?: run {
                    Log.d(TAG, "Token olmadığı için header eklenmedi")
                }
            }.build()
            try {
                val response = chain.proceed(newRequest)
                response
            } catch (e: Exception) {
                Log.e(TAG, "İstek sırasında hata oluştu", e)
                throw e
            }
        }
        Log.d(TAG, "Auth interceptor oluşturuldu")

        // OkHttpClient oluştur
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Önce auth interceptor
            .addInterceptor(loggingInterceptor) // Sonra logging interceptor
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        @Volatile
        private var instance: ApiClient? = null

        fun getInstance(context: Context): ApiClient {
            return instance ?: synchronized(this) {
                instance ?: ApiClient(context.applicationContext).also { instance = it }
            }
        }
        
        fun createService(): FileService {
            val retrofit = Retrofit.Builder()
                .baseUrl(Routes.Companion.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(FileService::class.java)
        }
    }
}
