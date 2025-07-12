package com.presetschool.momobookapp.service

import com.presetschool.momobookapp.model.Demo
import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.model.SmsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {
    @POST("add_transaction") // Endpoint
    fun createTransaction(@Body request: SmsRequest): Call<SmsResponse>

    @POST("records") // Endpoint
    fun getRecords(): Call<SmsResponse>

    @GET("demo")
    fun testNetwork(): Call<Demo>
}