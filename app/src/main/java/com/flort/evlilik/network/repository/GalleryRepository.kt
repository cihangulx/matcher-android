package com.flort.evlilik.network.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.flort.evlilik.models.auth.GalleryItem
import com.flort.evlilik.models.user.request.UpdateGalleryRequest
import com.flort.evlilik.network.ApiClient
import com.flort.evlilik.network.service.FileService
import com.flort.evlilik.network.service.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class GalleryRepository(private val context: Context) {
    
    private val userService: UserService = ApiClient.getInstance(context).userService
    private val fileService: FileService = ApiClient.getInstance(context).fileService
    
    companion object {
        private const val TAG = "GalleryRepository"
    }
    
    /**
     * Kullanıcının galeri verilerini API'den çeker
     */
    fun getGallery(): Flow<Result<List<GalleryItem>>> = flow {
        try {
            Log.d(TAG, "Galeri verileri çekiliyor...")
            val response = userService.getGallery()
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Galeri verileri başarıyla çekildi: ${response.data.gallery.size} öğe")
                emit(Result.success(response.data.gallery))
            } else {
                Log.e(TAG, "Galeri verileri çekilemedi: ${response.message}")
                emit(Result.failure(Exception(response.message ?: "Galeri verileri çekilemedi")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Galeri verileri çekilirken hata oluştu", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Galeri verilerini API'ye günceller
     */
    fun updateGallery(gallery: List<GalleryItem>): Flow<Result<List<GalleryItem>>> = flow {
        try {
            Log.d(TAG, "Galeri güncelleniyor: ${gallery.size} öğe")
            
            // Index field'ını temizle - sadece url ve isMain kullan
            val cleanGallery = gallery.map { item ->
                GalleryItem(
                    index = 0, // Index'i 0 yap, kullanmayacağız
                    url = item.url,
                    isMain = item.isMain
                )
            }
            
            val response = userService.updateGallery(
                UpdateGalleryRequest(cleanGallery)
            )
            
            if (response.success && response.data != null) {
                Log.d(TAG, "Galeri başarıyla güncellendi")
                emit(Result.success(response.data.gallery))
            } else {
                Log.e(TAG, "Galeri güncellenemedi: ${response.message}")
                emit(Result.failure(Exception(response.message ?: "Galeri güncellenemedi")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Galeri güncellenirken hata oluştu", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Dosyayı API'ye yükler
     */
    fun uploadFile(uri: Uri, folder: String = "gallery"): Flow<Result<String>> = flow {
        try {
            Log.d(TAG, "Dosya yükleniyor: $uri, klasör: $folder")
            
            // URI'yi File'a çevir
            val file = uriToFile(uri)
            if (file == null) {
                emit(Result.failure(Exception("Dosya oluşturulamadı")))
                return@flow
            }
            
            // MultipartBody oluştur
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            // API'ye yükle
            val response = fileService.uploadFile(body, folder)
            
            if (response.success && response.data != null && response.data.isNotEmpty()) {
                val uploadedFile = response.data.first()
                Log.d(TAG, "Dosya başarıyla yüklendi: ${uploadedFile.url}")
                emit(Result.success(uploadedFile.url))
            } else {
                Log.e(TAG, "Dosya yüklenemedi: ${response.message}")
                emit(Result.failure(Exception(response.message ?: "Dosya yüklenemedi")))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Dosya yüklenirken hata oluştu", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * URI'yi File'a çevirir
     */
    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "URI'den input stream açılamadı")
                return null
            }
            
            val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            Log.d(TAG, "URI File'a çevrildi: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "URI File'a çevrilirken hata oluştu", e)
            null
        }
    }
}
