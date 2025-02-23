package com.presetschool.momobookapp.model

import com.google.gson.annotations.SerializedName

data class SmsRequest(
    @SerializedName("datetime") val datetime: String,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("message") val message: String,
    @SerializedName("ref") val ref: String,
    @SerializedName("mref") val mref: String,
    @SerializedName("id") val id: Int,
    @SerializedName("sender") val sender: String,
    @SerializedName("type") val type: String,
)
