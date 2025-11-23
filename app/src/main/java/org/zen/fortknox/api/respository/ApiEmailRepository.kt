package org.zen.fortknox.api.respository

import org.zen.fortknox.api.base.response.OK
import org.zen.fortknox.api.entity.ApiEmail
import org.zen.fortknox.api.service.EmailService

class ApiEmailRepository(private val api: EmailService) {
    suspend fun send(apiEmail: ApiEmail): Result<ApiEmail> {
        return try {
            val response = api.send(apiEmail)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.status == OK) {
                    Result.success(apiResponse.data ?: apiEmail)
                } else {
                    Result.failure(Exception(apiResponse?.message ?: "Unknown error"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}