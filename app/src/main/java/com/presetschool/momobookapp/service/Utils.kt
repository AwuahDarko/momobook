package com.presetschool.momobookapp.service

import android.util.Log
import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.model.SmsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Utils {

    companion object {
        fun sendPostRequest() {
            val postRequest = SmsRequest(
                datetime = "Hello Retrofit",
                message = "This is a POST request example.",
                ref = "f",
                transactionId = ""
            )

            val call = RetrofitClient.instance.createPost(postRequest)
            call.enqueue(object : Callback<SmsResponse> {
                override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                    if (response.isSuccessful) {
                        Log.d("Retrofit", "Response: ${response.body()}")
                    } else {
                        Log.e("Retrofit", "Failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                }
            })
        }
    }
}