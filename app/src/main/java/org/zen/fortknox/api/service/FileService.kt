package org.zen.fortknox.api.service

import okhttp3.MultipartBody
import org.zen.fortknox.api.base.response.ApiResponse
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {

    @Multipart
    @POST("upload")
    suspend fun uploadUserImage(
        @Part file: MultipartBody.Part,
        @Part("fileName") fileName: String
    ): Response<ApiResponse<String>>
}