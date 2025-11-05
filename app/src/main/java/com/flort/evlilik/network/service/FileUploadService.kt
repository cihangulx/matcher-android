package com.flort.evlilik.network.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.flort.evlilik.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileUploadService(private val context: Context) {

    private val fileService: FileService = ApiClient.Companion.getInstance(context).fileService

    suspend fun uploadImage(
        imageUri: Uri,
        folder: String = "image",
        onProgress: (progress: Int) -> Unit = {}
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("FileUploadService", "Starting upload for URI: $imageUri")

                // URI'yi File'a çevir
                val file = uriToFile(imageUri)
                if (file == null) {
                    Log.e("FileUploadService", "Failed to convert URI to File")
                    return@withContext Result.failure(Exception("Dosya dönüştürülemedi"))
                }

                Log.d(
                    "FileUploadService",
                    "File created: ${file.absolutePath}, size: ${file.length()}"
                )

                // Multipart request body oluştur
                val requestFile = file.asRequestBody("image/*".toMediaType())
                val filePart = MultipartBody.Part.createFormData(
                    "file1",
                    file.name,
                    requestFile
                )

                // Upload işlemini başlat
                val response = fileService.uploadFile(
                    file = filePart,
                    folder = folder
                )

                if (response.success && response.data != null && response.data.isNotEmpty()) {
                    val uploadedFile = response.data.first()
                    Log.d("FileUploadService", "Upload successful: ${uploadedFile.url}")
                    Result.success(uploadedFile.url)
                } else {
                    Log.e("FileUploadService", "Upload failed: ${response.message}")
                    Result.failure(Exception(response.message ?: "Upload failed"))
                }

            } catch (e: Exception) {
                Log.e("FileUploadService", "Upload error", e)
                Result.failure(e)
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("FileUploadService", "Cannot open input stream for URI: $uri")
                return null
            }

            // Geçici dosya oluştur
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)

            // Dosyayı kopyala
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            Log.d("FileUploadService", "File created successfully: ${tempFile.absolutePath}")
            tempFile

        } catch (e: Exception) {
            Log.e("FileUploadService", "Error converting URI to File", e)
            null
        }
    }
}