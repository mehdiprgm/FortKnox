package org.zen.fortknox.viewmodel

import androidx.lifecycle.ViewModel
import org.zen.fortknox.api.entity.ApiEmail
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.api.respository.ApiEmailRepository
import org.zen.fortknox.api.respository.ApiUserRepository
import org.zen.fortknox.api.base.RetrofitClient
import org.zen.fortknox.api.base.response.ApiResponse
import org.zen.fortknox.api.respository.ApiFileRepository
import java.io.File

class ApiViewModel : ViewModel() {
    private var apiUserRepository: ApiUserRepository = ApiUserRepository(RetrofitClient.userService)
    private var apiEmailRepository: ApiEmailRepository = ApiEmailRepository(RetrofitClient.emailService)
    private var apiFileRepository: ApiFileRepository = ApiFileRepository(RetrofitClient.fileService)

    suspend fun addUser(apiUser: ApiUser): Result<ApiUser> {
        return apiUserRepository.add(apiUser)
    }

    suspend fun updateUser(username: String, apiUser: ApiUser): Result<ApiUser> {
        return apiUserRepository.update(username, apiUser)
    }

    suspend fun getUser(username: String): Result<ApiUser> {
        return apiUserRepository.get(username)
    }

    suspend fun getAllUsers(): Result<List<ApiUser>> {
        return apiUserRepository.getAll()
    }

    suspend fun uploadImage(localFile: File, fileName: String) : Result<ApiResponse<String>> {
        return apiFileRepository.uploadImage(localFile, fileName)
    }

    suspend fun sendVerificationEmail(apiEmail: ApiEmail): Result<ApiEmail> {
        return apiEmailRepository.send(apiEmail)
    }
}