package com.presetschool.momobookapp.service

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import com.presetschool.momobookapp.model.Demo
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.model.SmsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Utils {

    companion object {
        const val isPreset: Boolean = true

        fun sendPostRequest(
            postRequest: SmsRequest, success: (result: String) -> Unit,
            failure: (result: String) -> Unit
        ) {

            val call = RetrofitClient.instance.createTransaction(postRequest)
            call.enqueue(object : Callback<SmsResponse> {
                override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                    if (response.isSuccessful) {
                        val res = response.body() as SmsResponse;

                        if (res.status == 200 || res.status == 409) {
                            success(res.message)
                        }else{
                            failure(res.message)
                        }
                    } else {
                        failure(response.message())
                    }
                }

                override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    t.message?.let { failure(it) }
                }
            })
        }

        fun sendGetRequest(
             success: (result: String) -> Unit,
            failure: (result: String) -> Unit
        ) {

            val call = RetrofitClient.instance.getRecords()
            call.enqueue(object : Callback<SmsResponse> {
                override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                    if (response.isSuccessful) {
                        val res = response.body() as SmsResponse;

                        if (res.status == 200 || res.status == 409) {
                            success(res.message)
                        }else{
                            failure(res.message)
                        }
                    } else {
                        Log.e("Retrofit", "Failed: ${response.message()}")
                        failure(response.message())
                    }
                }

                override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    t.message?.let { failure(it) }
                }
            })
        }

        fun sendTest(
            success: (result: String) -> Unit,
            failure: (result: String) -> Unit
        ) {

            val call = RetrofitClient.instance.testNetwork()
            call.enqueue(object : Callback<Demo> {
                override fun onResponse(call: Call<Demo>, response: Response<Demo>) {

                    if (response.isSuccessful) {
                        val res = response.body() as Demo;
                        success(res.message)

                    } else {
                        Log.e("Retrofit", "Failed: ${response.message()}")
                        failure(response.message())
                    }
                }

                override fun onFailure(call: Call<Demo>, t: Throwable) {
                    Log.e("Retrofit", "Error: ${t.message}")
                    t.message?.let { failure(it) }
                }
            })
        }

        fun messageType(message: Message): MessageType {
            if(!isPreset){
                val body = message.body.replace("\n", " ").substring(0, 85)
                return if (body.lowercase().contains("you have paid") ) {
                    MessageType.EXPENSE
                } else if (body.lowercase().contains("you have received")) {
                    MessageType.INCOME
                } else {
                    MessageType.NONE
                }
            }

            val body = message.body.substring(0, 20)
            return if (body.lowercase().contains("payment for") ||
                body.lowercase().contains("payment made for") ||
                body.lowercase().contains("your payment of")
                ) {
                MessageType.EXPENSE
            } else if (body.lowercase().contains("payment received") or body.lowercase().contains("an amount of")) {
                MessageType.INCOME
            } else {
                MessageType.NONE
            }
        }

        fun extractAmount(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            try {
                val list = message.body.split("GHS")
                val secondPart = list[1]

                if (messageType(message) == MessageType.INCOME) {

                    val temp = secondPart.split("from")
                    if(temp.size > 1){
                        val money = temp[0]
                        return money.trim()
                    }
                   val money = secondPart.split("has")[0]
                    return money.trim()
                }

                if (messageType(message) == MessageType.EXPENSE) {
                    val money = secondPart.split("to")[0]
                    return money.trim()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }

        fun extractRef(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            try {
                if (messageType(message) == MessageType.EXPENSE) {
                    if(!isPreset){
                        return "";
                    }

                    if(message.body.contains("Reference:")){
                        val bod = message.body.split("Reference:")[1]
                        val refSplitList = bod.split("Transaction ID:")
                        if(refSplitList.size > 1){
                            val ref = bod.split("Transaction ID:")[0]
                            return ref.trim().replace(".", "")
                        }

                        val firstPart = bod.split("Transaction Id:")[0]
                        val ref = firstPart.split(".")[0]
                        return ref.trim().replace(".", "").trim()
                    }

                    val list = message.body.split("GHS")
                    val secondPart = list[1]
                    var ref = secondPart.split("to")[1]
                    ref = ref.replace(".Current Balance:", "").trim()
                    return ref.replace("Current Balance:", "").trim().replace(".", "")
                }

                if (messageType(message) == MessageType.INCOME) {
                    if(!isPreset){
                        val list = message.body.split("from")
                        val secondPart = list[1]
                        val ref = secondPart.split("to WONDER HEIGHTS INTERNATIONAL SCHOOL")[0]
                        return ref.trim().replace(".", "")
                    }

                    val list = message.body.split("Reference:")
                    val secondPart = list[1]
                    val ref = secondPart.split("Transaction")[0]
                    return ref.trim().replace(".", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun extractSender(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            if(!isPreset) return extractRef(message)

            try {
                val list = message.body.split("GHS")
                val secondPart = list[1]

                if (messageType(message) == MessageType.EXPENSE) {
                    var send = secondPart.split("to")[1]
                    if(!send.contains("Current Balance:")){
                        return ""
                    }

                    send = send.replace(".Current Balance:", "").trim()
                    return send.replace("Current Balance:", "").trim()
                }

                if (messageType(message) == MessageType.INCOME) {
                    val temp = secondPart.split("from")
                    if (temp.size > 1){
                        var send = temp[1]
                        send = send.replace(".Current Balance:", "").trim()
                        return send.replace("Current Balance:", "").trim()
                    }

                    return "MTN INTEREST"
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }

        fun extractTransactionId(message: Message): String {
            if (messageType(message) == MessageType.NONE) return ""

            try {
                if(!isPreset){
                    val list = message.body.split("Txn:")
                    val secondPart = list[1]
                    return secondPart.replace("Thank you", "").replace(".", "").trim()
                }

                var list = message.body.split("Transaction ID:")
                if (list.size == 1) {
                    list = message.body.split("Transaction Id:")
                }
                val secondPart = list[1]

                if (messageType(message) == MessageType.EXPENSE) {
                    val li = secondPart.split("Fee charged:")
                    if(li.size > 1){
                        val trans = secondPart.split("Fee charged:")[0]
                        return trans.replace(".", "").trim()
                    }

                    val trans = secondPart.split("External")[0]
                    return trans.replace(".", "").trim()

                }

                if (messageType(message) == MessageType.INCOME) {

                    var trans = secondPart.split("TRANSACTION")[0]
                    if(trans.contains("Fee")){
                        trans = trans.split("Fee")[0]
                    }
                    return trans.replace(".", "").trim()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }
    }
}