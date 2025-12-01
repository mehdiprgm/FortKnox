package org.zen.fortknox.api.service

import okhttp3.MultipartBody
import org.zen.fortknox.api.base.response.ApiResponse
import org.zen.fortknox.api.entity.ApiUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserService {

    // GET all users
    @GET("users")
    suspend fun getAll(): Response<List<ApiUser>>

    // GET user by username
    @GET("users/{username}")
    suspend fun get(@Path("username") username: String): Response<ApiUser>

    // POST add new user
    @POST("users/add")
    suspend fun add(@Body user: ApiUser): Response<ApiUser>

    // PUT update user by username
    @PUT("users/{username}")
    suspend fun update(
        @Path("username") username: String, @Body user: ApiUser
    ): Response<ApiUser>

    // GET users count
    @GET("users/count")
    suspend fun count(): Response<ApiResponse<Int>>
}