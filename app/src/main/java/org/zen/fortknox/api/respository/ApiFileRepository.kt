package org.zen.fortknox.api.respository

import org.zen.fortknox.api.base.response.ApiResponse
import org.zen.fortknox.api.service.FileService
import org.zen.fortknox.tools.getStackTraceAsString
import org.zen.fortknox.tools.prepareFilePart
import java.io.File
import java.io.IOException

class ApiFileRepository(private val api: FileService) {
    suspend fun uploadImage(localFile: File, fileName: String): Result<ApiResponse<String>> {
        return try {
            val filePart = prepareFilePart(localFile, "file")

            val response = api.uploadUserImage(filePart, fileName)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null) {
                    Result.success(apiResponse)
                } else {
                    Result.failure(IOException("Server returned success (HTTP 200) but the API response body was empty."))
                }
            } else {
                val errorBody = response.errorBody()?.string()

                val errorApiResponse = ApiResponse<String>(
                    message = "HTTP Error ${response.code()}: ${errorBody ?: "Unknown network error."}",
                    status = response.code(),
                    data = null
                )

                Result.success(errorApiResponse)
            }
        } catch (e: Exception) {
            Result.failure(
                IOException(
                    "Network or Unexpected Error: ${e.getStackTraceAsString()}", e
                )
            )
        }
    }
}