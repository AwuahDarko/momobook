package com.presetschool.momobookapp.model

import com.google.gson.annotations.SerializedName
data class SmsRequest(
    @SerializedName("datetime") val datetime: String,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("message") val message: String,
    @SerializedName("ref") val ref: String
)
