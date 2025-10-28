package com.matcher.matcher.network.service

import com.matcher.matcher.network.Routes
import com.matcher.matcher.models.file.FileUploadResponse
import com.matcher.matcher.network.model.ApiResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {

    @Multipart
    @POST(Routes.UPLOAD_FILE)
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: String
    ): ApiResponse<List<FileUploadResponse>>
}