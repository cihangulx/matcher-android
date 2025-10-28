package com.matcher.matcher.network.model

import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/**
 * API hatalarını handle eden utility sınıfı
 * 
 * HTTP status kodları 200 dışında olduğunda API'den gelen
 * error mesajlarını parse eder ve kullanıcıya gösterir.
 */
object ApiException {
    
    /**
     * Exception'dan kullanıcıya gösterilecek hata mesajını çıkarır
     */
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is HttpException -> {
                // HTTP hatası (400, 401, 404, 500, vb.)
                parseHttpError(exception)
            }
            is IOException -> {
                // Ağ bağlantı hatası
                "Bağlantı hatası. İnternet bağlantınızı kontrol edin."
            }
            else -> {
                // Diğer hatalar
                exception.message ?: "Beklenmeyen bir hata oluştu"
            }
        }
    }
    
    /**
     * HTTP exception'dan error body'yi parse eder
     */
    private fun parseHttpError(exception: HttpException): String {
        return try {
            val errorBody = exception.response()?.errorBody()?.string()
            
            if (!errorBody.isNullOrEmpty()) {
                // API'den gelen error response'u parse et
                val errorResponse = Gson().fromJson(errorBody, ApiResponse::class.java)
                errorResponse.message ?: getDefaultErrorMessage(exception.code())
            } else {
                getDefaultErrorMessage(exception.code())
            }
        } catch (e: Exception) {
            // Parse hatası durumunda default mesaj
            getDefaultErrorMessage(exception.code())
        }
    }
    
    /**
     * HTTP status koduna göre varsayılan hata mesajı döner
     */
    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Geçersiz istek. Lütfen bilgilerinizi kontrol edin."
            401 -> "Oturum süreniz doldu. Lütfen tekrar giriş yapın."
            403 -> "Bu işlem için yetkiniz yok."
            404 -> "İstenen kaynak bulunamadı."
            408 -> "İstek zaman aşımına uğradı. Lütfen tekrar deneyin."
            422 -> "Girdiğiniz bilgiler geçerli değil."
            429 -> "Çok fazla istek gönderdiniz. Lütfen biraz bekleyin."
            500 -> "Sunucu hatası. Lütfen daha sonra tekrar deneyin."
            502 -> "Sunucuya ulaşılamıyor. Lütfen daha sonra tekrar deneyin."
            503 -> "Servis şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin."
            else -> "Bir hata oluştu (Kod: $statusCode)"
        }
    }
}

