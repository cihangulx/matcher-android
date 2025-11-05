package com.flort.evlilik.network.service

import com.flort.evlilik.network.Routes
import com.flort.evlilik.models.file.FileUploadResponse
import com.flort.evlilik.network.model.ApiResponse
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