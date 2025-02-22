package com.presetschool.momobookapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import java.time.LocalTime


class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle["pdus"] as? Array<*>
                    val format = bundle.getString("format") // Get the SMS format (GSM/CDMA)

                    pdus?.forEach { pdu ->
                        val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray, format) // ✅ Use the new method
                        val sender = smsMessage.originatingAddress
                        val messageBody = smsMessage.messageBody



                        Log.e("SmsReceiver", "SMS received from: $sender, Message: $messageBody")

                        // Show a toast notification (or update UI)
                        Toast.makeText(context, "New SMS from $sender: $messageBody", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("SmsReceiver", "Error reading SMS: ${e.message}")
                }
            }
        }
    }


}