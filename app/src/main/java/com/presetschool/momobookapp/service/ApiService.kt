package com.presetschool.momobookapp.service

import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.model.SmsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiService {
    @POST("add_transaction") // Endpoint
    fun createTransaction(@Body request: SmsRequest): Call<SmsResponse>
}