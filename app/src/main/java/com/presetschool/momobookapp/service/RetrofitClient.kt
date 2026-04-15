package com.presetschool.momobookapp.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://wonderheightsschool.com/"

    val instance: ApiService by lazy {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    // Add other headers as needed
                     .header("Authorization", "Bearer KazKzoF7HWu0PMWebvH4iVyZszhheww3CkuHPfThNnp6yMriPS")
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Add the client here
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON automatically
            .build()
            .create(ApiService::class.java)
    }


}