package com.presetschool.momobookapp.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.presetschool.momobookapp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmsReader {

    fun readSms(context: Context, filterDate: String): List<Message> {
        val smsList = mutableListOf<Message>()
        val senderId = "MobileMoney"


        try {
            val uri: Uri = Uri.parse("content://sms/inbox") // Inbox messages
            val projection = arrayOf("_id", "address", "date", "body")

            // 🟢 Convert filterDate (String) to a timestamp
            val filterTimestamp = convertDateToTimestamp(filterDate)

            // 🟢 Filter SMS where sender is "VANY" AND date is after filterTimestamp
            val selection = "address LIKE ? AND date >= ?"
            val selectionArgs = arrayOf("%$senderId%", filterTimestamp.toString())

            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, "date DESC")

            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                val idIndex = it.getColumnIndex("_id")

                while (it.moveToNext()) {
                    val sender = if (addressIndex != -1) it.getString(addressIndex) else "Unknown"
                    val message = if (bodyIndex != -1) it.getString(bodyIndex) else "No Content"
                    val timestamp = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                    val id = if (idIndex != -1) it.getLong(idIndex) else 0

                    // 🟢 Convert timestamp to a readable date format
                    val formattedDate = formatDate(timestamp)
                    val formattedDate2 = formatDate2(timestamp)



                    smsList.add(Message(
                        apiDate = formattedDate,
                        displayDate = formattedDate2,
                        body = message,
                        id = id,
                        address = sender
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReader", "Error reading SMS: ${e.message}")
//            Toast.makeText(context, "Error reading SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }

        return smsList
    }

    fun readPendingSms(context: Context, filterDate: String, filterId: String):List<Message> {
        val smsList = mutableListOf<Message>()
        val senderId = "MobileMoney"

        try {
            val uri: Uri = Uri.parse("content://sms/inbox") // Inbox messages
            val projection = arrayOf("_id", "address", "date", "body")

            // 🟢 Convert filterDate (String) to a timestamp
            val filterTimestamp = convertDateToTimestamp(filterDate)

            // 🟢 Filter SMS where sender is "VANY" AND date is after filterTimestamp
            val selection = "address LIKE ? AND date >= ? AND _id not in ($filterId)"
            val selectionArgs = arrayOf("%$senderId%", filterTimestamp.toString())

            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, "date ASC")

            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                val idIndex = it.getColumnIndex("_id")

                while (it.moveToNext()) {
                    val sender = if (addressIndex != -1) it.getString(addressIndex) else "Unknown"
                    val message = if (bodyIndex != -1) it.getString(bodyIndex) else "No Content"
                    val timestamp = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                    val id = if (idIndex != -1) it.getLong(idIndex) else 0

                    // 🟢 Convert timestamp to a readable date format
                    val formattedDate = formatDate(timestamp)
                    val formattedDate2 = formatDate2(timestamp)

                    smsList.add(Message(
                        apiDate = formattedDate,
                        displayDate = formattedDate2,
                        body = message,
                        id = id,
                        address = sender
                    ))
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReader", "Error reading SMS: ${e.message}")
//            Toast.makeText(context, "Error reading SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }

        return smsList
    }


    fun readNextPendingSms(context: Context, filterDate: String, filterId: String): ArrayList<Message?>{
        val smses:ArrayList<Message?> = ArrayList()
        val senderId = "MobileMoney"

        try {
            val uri: Uri = Uri.parse("content://sms/inbox") // Inbox messages
            val projection = arrayOf("_id", "address", "date", "body")

            // 🟢 Convert filterDate (String) to a timestamp
            val filterTimestamp = convertDateToTimestamp(filterDate)

            // 🟢 Filter SMS where sender is "VANY" AND date is after filterTimestamp
            val selection = "address LIKE ? AND date >= ? AND _id not in ($filterId)"
            val selectionArgs = arrayOf("%$senderId%", filterTimestamp.toString())

            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, " date ASC ")

            cursor?.use {
                val addressIndex = it.getColumnIndex("address")
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                val idIndex = it.getColumnIndex("_id")

                while (it.moveToNext()) {
                    val sender = if (addressIndex != -1) it.getString(addressIndex) else "Unknown"
                    val message = if (bodyIndex != -1) it.getString(bodyIndex) else "No Content"
                    val timestamp = if (dateIndex != -1) it.getLong(dateIndex) else 0L
                    val id = if (idIndex != -1) it.getLong(idIndex) else 0

                    // 🟢 Convert timestamp to a readable date format
                    val formattedDate = formatDate(timestamp)
                    val formattedDate2 = formatDate2(timestamp)

                    smses.add(
                        Message(
                        apiDate = formattedDate,
                        displayDate = formattedDate2,
                        body = message,
                        id = id,
                        address = sender)
                    )

                }
            }
        } catch (e: Exception) {
            Log.e("SmsReader", "Error reading SMS: ${e.message}")
//            Toast.makeText(context, "Error reading SMS: ${e.message}", Toast.LENGTH_LONG).show()
        }

        return smses
    }

    // Function to format timestamp into a readable date and time
    private fun formatDate(timestamp: Long): String {
        return if (timestamp > 0) {
//            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown Date"
        }
    }

    private fun formatDate2(timestamp: Long): String {
        return if (timestamp > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
//            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.format(Date(timestamp))
        } else {
            "Unknown Date"
        }
    }


    private fun convertDateToTimestamp(dateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateString)
            date?.time ?: 0L
        } catch (e: Exception) {
            Log.e("SmsReader", "Invalid date format: $dateString")
            0L
        }
    }


}