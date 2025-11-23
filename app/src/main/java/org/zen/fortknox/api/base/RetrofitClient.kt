package org.zendev.keepergen.api.base

import okhttp3.OkHttpClient
import org.zen.fortknox.api.service.EmailService
import org.zen.fortknox.api.service.UserService
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://keepergen-app.liara.run/"

    private val retrofit: Retrofit by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val emailService: EmailService by lazy {
        retrofit.create(EmailService::class.java)
    }
}