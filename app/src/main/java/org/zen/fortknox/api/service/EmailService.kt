package org.zen.fortknox.api.service

import org.zen.fortknox.api.base.response.ApiResponse
import org.zen.fortknox.api.entity.ApiEmail
import org.zen.fortknox.api.entity.ApiUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmailService {
    /* Send verification code to email */
    @POST("Email/sendCode")
    suspend fun send(@Body email: ApiEmail): Response<ApiResponse<ApiEmail>>
}