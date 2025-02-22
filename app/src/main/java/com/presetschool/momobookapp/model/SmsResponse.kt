package com.presetschool.momobookapp.model

import com.google.gson.annotations.SerializedName

data class SmsResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
)
