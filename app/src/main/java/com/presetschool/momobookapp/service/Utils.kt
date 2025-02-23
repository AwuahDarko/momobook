package com.presetschool.momobookapp.service

import android.util.Log
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageType
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
                mref = "",
                transactionId = "",
                sender = "",
                id = 0,
                type = ""
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

        fun messageType(message: Message): MessageType {
            val body = message.body.substring(0, 20)
            return if (body.lowercase().contains("payment for") or body.lowercase().contains("payment made for")) {
                MessageType.EXPENSE
            } else if (body.lowercase().contains("payment received")) {
                MessageType.INCOME
            } else {
                MessageType.NONE
            }
        }

        fun extractAmount(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            val list = message.body.split("GHS")
            val secondPart = list[1]

            if (messageType(message) == MessageType.INCOME) {
                val money = secondPart.split("from")[0]
                return money.trim()
            }

            if (messageType(message) == MessageType.EXPENSE) {
                val money = secondPart.split("to")[0]
                return money.trim()
            }

            return ""
        }

        fun extractRef(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            // TODO === redo it ===
            if (messageType(message) == MessageType.EXPENSE) {
                val list = message.body.split("GHS")
                val secondPart = list[1]
                val ref = secondPart.split("to")[1]
                return ref.trim()
            }

            if (messageType(message) == MessageType.INCOME) {

            }
            return ""
        }
    }
}