package com.presetschool.momobookapp.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ProcessLifecycleOwner
import com.presetschool.momobookapp.MainActivity
import com.presetschool.momobookapp.R
import com.presetschool.momobookapp.model.LocalItem
import com.presetschool.momobookapp.model.Message
import com.presetschool.momobookapp.model.MessageSentEvent
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.model.SmsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.LocalDate
import kotlin.math.absoluteValue

class ForegroundService: Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val channelId = "foreground_service_channel"
    private lateinit var sharedPreference: SharedPreferencesHelper
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
         sharedPreference = SharedPreferencesHelper(this)
        dbHelper = DatabaseHelper(this)

        val startDate = sharedPreference.getString("start_date")
        if (startDate.isEmpty()) {
            sharedPreference.saveString("start_date", LocalDate.now().toString())
        }

        val delayTime: Int = sharedPreference.getInt("delay_time", 0)
        if (delayTime == 0) {
            sharedPreference.saveInt("delay_time", 5)
        }

//        if (Settings.canDrawOverlays(this)) {
//            showOverlay()
//        } else {
//        }

        // 🔹 Register lifecycle listener
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(this))
    }

    private fun startForegroundService() {

        val channel = NotificationChannel(
            channelId, "Foreground Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

    }

    fun showOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.LEFT

        params.x = 100 // Start position
        params.y = 100


        // 🔹 Handle click event to open the MainActivity
        overlayView?.setOnClickListener {

            val packageName = packageName
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.appTasks

            if (tasks.isNotEmpty()) {
                // 🔹 If the app is already running, bring it to the front
                tasks[0].moveToFront()
            } else {
                // 🔹 If the app is NOT running, launch it
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            }
        }

        // 🔹 Enable dragging
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f
            private var isClick = false
            private val clickThreshold = 10 // Distance to consider as click

            override fun onTouch(view: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        touchX = event.rawX
                        touchY = event.rawY
                        isClick = true // Assume it's a click
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - touchX).toInt()
                        val dy = (event.rawY - touchY).toInt()

                        if (dx.absoluteValue > clickThreshold || dy.absoluteValue > clickThreshold) {
                            isClick = false // If moved beyond threshold, it's a drag
                        }

                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(overlayView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isClick) {
                            view.performClick() // Ensure click event is triggered
                        }
                        return true
                    }
                }
                return false
            }
        })

        windowManager?.addView(overlayView, params)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Overlay Service")
            .setContentText("Running in the background")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)

        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {

                val time = sharedPreference.getInt("delay_time").toLong()
                sendData()
                delay(1000 * 60 * time)
//                delay(1000  * time)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { windowManager?.removeView(it) }
    }


    fun hideOverlay() {
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
    }


    private suspend fun sendData() {

//        val sharedPreference = SharedPreferencesHelper(this)

        val originalDateStr = sharedPreference.getString("start_date")

        val list = originalDateStr.split("-")
        val filterDate = "${list[2]}/${list[1]}/${list[0]}"

        val items = dbHelper.getAllItems()
        val ids: ArrayList<String> = ArrayList()
        for (item in items) {
            ids.add(item._id)
        }

        var IDs: String = ids.joinToString(",")

        if (IDs.isEmpty()) IDs = "0"

        val smses: ArrayList<Message?> = SmsReader.readNextPendingSms(this, filterDate, IDs)


        for (sms in smses){
            if (sms?.let { Utils.messageType(it) } == MessageType.NONE) {
                dbHelper.addItem(LocalItem(sms.id.toString(), "", sms.body))
                return
            }

            if (sms != null) {
                val amt = Utils.extractAmount(sms)
                val ref = Utils.extractRef(sms)
                val sender = Utils.extractSender(sms)
                val transactionId = Utils.extractTransactionId(sms)

                val type = if (Utils.messageType(sms) == MessageType.INCOME) "INCOME" else if (Utils.messageType(sms) == MessageType.EXPENSE) "EXPENSE" else "NONE"

                val postRequest = SmsRequest(
                    datetime = sms.apiDate,
                    message = sms.body,
                    ref = ref,
                    mref = "",
                    transactionId = transactionId,
                    sender = sender,
                    id = sms.id.toInt(),
                    type = type,
                    amount = amt,
                    includeInAccount = 1,
                    useTimes = "1",
                    from = "preset"
                )

                Utils.sendPostRequest(postRequest,
                    success = { msg ->
                        dbHelper.addItem(LocalItem(sms.id.toString(), "", sms.body))
                        EventBus.getDefault().post(MessageSentEvent())
                    }, failure = { msg ->
                    })


//            Utils.sendTest(
//                success = { msg ->
//                    Log.d("TEST RESULTS", msg)
//                    EventBus.getDefault().post(MessageSentEvent())
//                }, failure = { msg ->
//                    Log.d("TEST FAILED", msg)
//                })

                // don't overwhelm the server
                delay(500)
            }
        }




//        val postRequest = sms?.let {
////            val type = Utils.messageType(it)
//            val amt = Utils.extractAmount(it)
//            val ref = Utils.extractRef(it)
//            val sender = Utils.extractSender(it)
//            val transactionId = Utils.extractTransactionId(it)
//
//            val type: String = if (Utils.messageType(it) == MessageType.INCOME) "INCOME" else "EXPENSE"
//
//            SmsRequest(
//                datetime = it.apiDate,
//                message = sms.body,
//                ref = ref,
//                mref = "",
//                transactionId = transactionId,
//                sender = sender,
//                id = sms.id.toInt(),
//                type = type,
//                amount = amt
//            )
//        }


//        if (postRequest != null) {
//            Utils.sendPostRequest(postRequest,
//                success = { msg ->
//                    dbHelper.addItem( LocalItem(item.id.toString(), mref, item.body))
//                }, failure = { msg ->
//
//                })
//        }
    }
}