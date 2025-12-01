package org.zen.fortknox.api.respository

import org.zen.fortknox.api.base.response.ApiResponse
import org.zen.fortknox.api.service.UserService
import org.zen.fortknox.api.base.response.CONFLICT
import org.zen.fortknox.api.base.response.INVALID_REQUEST
import org.zen.fortknox.api.base.response.NOT_FOUND
import org.zen.fortknox.api.base.response.OK
import org.zen.fortknox.api.base.response.SERVER_ERROR
import org.zen.fortknox.api.entity.ApiUser
import org.zen.fortknox.tools.AppException
import org.zen.fortknox.tools.getStackTraceAsString
import org.zen.fortknox.tools.prepareFilePart
import java.io.File
import java.io.IOException

class ApiUserRepository(private val api: UserService) {
    suspend fun getAll(): Result<List<ApiUser>> {
        return try {
            val response = api.getAll()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to fetch users: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(AppException.NetworkException("Check your internet connection", e))
        } catch (e: Exception) {
            Result.failure(AppException.UnexpectedException("Something went wrong", e))
        }
    }

    suspend fun get(username: String): Result<ApiUser> {
        return try {
            val response = api.get(username)
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(AppException.UserNotFoundException("User '$username' not found"))
                }
            } else {
                when (response.code()) {
                    NOT_FOUND -> Result.failure(AppException.UserNotFoundException("User '$username' not found"))
                    SERVER_ERROR -> Result.failure(
                        AppException.ApiException(
                            "Server error", SERVER_ERROR
                        )
                    )

                    else -> Result.failure(
                        AppException.ApiException(
                            "HTTP ${response.code()}: ${response.message()}", response.code()
                        )
                    )
                }
            }
        } catch (e: IOException) {
            Result.failure(AppException.NetworkException("Check your internet connection.\n{${e.getStackTraceAsString()}", e))
        } catch (e: Exception) {
            Result.failure(AppException.UnexpectedException("Something went wrong", e))
        }
    }

    suspend fun add(user: ApiUser): Result<ApiUser> {
        return try {
            val response = api.add(user)
            if (response.isSuccessful) {
                val newUser = response.body()
                if (newUser != null) {
                    Result.success(newUser)
                } else {
                    Result.failure(Exception("Failed to create user"))
                }
            } else {
                when (response.code()) {
                    INVALID_REQUEST -> Result.failure(
                        AppException.ApiException(
                            "Invalid user data", INVALID_REQUEST
                        )
                    )

                    CONFLICT -> Result.failure(AppException.UserConflictException("User already exists"))
                    else -> Result.failure(Exception("Failed to create user: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(AppException.NetworkException("Check your internet connection", e))
        } catch (e: Exception) {
            Result.failure(AppException.UnexpectedException("Something went wrong", e))
        }
    }

    suspend fun update(username: String, user: ApiUser): Result<ApiUser> {
        return try {
            val response = api.update(username, user)
            if (response.isSuccessful) {
                val updatedUser = response.body()
                if (updatedUser != null) {
                    Result.success(updatedUser)
                } else {
                    Result.failure(Exception("Failed to update user"))
                }
            } else {
                when (response.code()) {
                    INVALID_REQUEST -> Result.failure(Exception("Invalid user data"))
                    NOT_FOUND -> Result.failure(Exception("User not found"))
                    else -> Result.failure(Exception("Failed to update user: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    suspend fun count(): Result<Int> {
        return try {
            val response = api.count()
            if (response.isSuccessful) {
                val count = response.body()?.data
                if (count != null) {
                    Result.success(count)
                } else {
                    Result.failure(Exception("Failed to get users count"))
                }
            } else {
                Result.failure(Exception("Failed to get users count: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }
}