package com.presetschool.momobookapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.presetschool.momobookapp.service.ForegroundService
import com.presetschool.momobookapp.service.SharedPreferencesHelper
import com.presetschool.momobookapp.service.Utils
import java.time.LocalDate
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_login)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }

    private lateinit var fingerprintIcon: ImageView
    private lateinit var statusText: TextView
    private lateinit var errorText: TextView
    private lateinit var appLogo: ImageView


    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var executor: Executor

    // Set click listener for fingerprint icon to trigger authentication
    // TODO ========
    private val enableBtn = true
//    private lateinit var dbHelper: DatabaseHelper
    private val SMS_PERMISSION_REQUEST = 101
    private val FOREGROUND_PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreference = SharedPreferencesHelper(this.baseContext)
//        dbHelper = DatabaseHelper(this)
//        dbHelper.deleteAllItems()

//        dbHelper.addItem(LocalItem())


        val startDate = sharedPreference.getString("start_date")
        if (startDate.isEmpty()) {
            sharedPreference.saveString("start_date", LocalDate.now().toString())
        }

        val delayTime: Int = sharedPreference.getInt("delay_time", 0)
        if (delayTime == 0) {
            sharedPreference.saveInt("delay_time", 5)
        }

        initializeViews()
        setupBiometricAuthentication()
        checkBiometricSupport()

        startService(Intent(this, ForegroundService::class.java))
        startOverlayService()

        checkAndRequestPermissions()

//        CoroutineScope(Dispatchers.Main).launch {
//            while (isActive) {
//
//                val time = sharedPreference.getInt("delay_time").toLong()
//                sendData()
////                delay(1000 * 60 * time)
//                delay(1000  * time)
//            }
//        }
    }

    private fun initializeViews() {
        fingerprintIcon = findViewById(R.id.fingerprint_icon)
        statusText = findViewById(R.id.status_text)
        errorText = findViewById(R.id.error_text)
        appLogo = findViewById(R.id.app_logo)

        appLogo.setImageResource(R.drawable.wonder)



        // Set click listener for fingerprint icon to trigger authentication
        // TODO ========
        fingerprintIcon.setOnClickListener {
            onLoginSuccess()
//            showBiometricPrompt()
        }
    }

    private fun setupBiometricAuthentication() {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showError(errString.toString())
                    resetFingerprintState()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onLoginSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showError("Authentication failed. Please try again.")
                    resetFingerprintState()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use your fingerprint to sign in")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                statusText.text = "Touch the fingerprint sensor"
                fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.fingerprint_color))
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                showError("Biometric authentication not available on this device")
                disableFingerprintLogin()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                showError("Biometric authentication currently unavailable")
                disableFingerprintLogin()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                showError("No biometric credentials enrolled. Please set up fingerprint in Settings.")
                disableFingerprintLogin()
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                showError("Biometric authentication unavailable - security update required")
                disableFingerprintLogin()
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                showError("Biometric authentication not supported")
                disableFingerprintLogin()
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                showError("Biometric authentication status unknown")
                disableFingerprintLogin()
            }

            else -> {
                showError("Biometric authentication not available")
                disableFingerprintLogin()
            }
        }
    }

    private fun showBiometricPrompt() {
        hideError()
        statusText.text = "Place your finger on the sensor"
        fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.fingerprint_active))
        biometricPrompt.authenticate(promptInfo)
    }

    private fun onLoginSuccess() {
        statusText.text = "Authentication successful!"
        fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.success_color))

        // Navigate to main activity after successful authentication
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.error_color))
    }

    private fun hideError() {
        errorText.visibility = View.GONE
    }

    private fun resetFingerprintState() {
        statusText.text = "Touch the fingerprint sensor"
        fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.fingerprint_color))
    }

    private fun disableFingerprintLogin() {
        fingerprintIcon.setColorFilter(ContextCompat.getColor(this, R.color.disabled_color))
        fingerprintIcon.isEnabled = enableBtn
        statusText.text = "Fingerprint login unavailable"
    }

    override fun onResume() {
        super.onResume()
        // Auto-trigger biometric prompt when activity resumes
        if (BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            == BiometricManager.BIOMETRIC_SUCCESS
        ) {
            showBiometricPrompt()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == FOREGROUND_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            startMyForegroundService()
//            Log.d("FOREGROUND_PERMISSION_REQUEST", "FOREGROUND_PERMISSION_REQUEST")
//            Toast.makeText(this, "Permission done for background sync", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied for background sync", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST
            )
        } else {
//            Toast.makeText(this, "SMS Permission Already Granted!", Toast.LENGTH_SHORT).show()
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC),
                    FOREGROUND_PERMISSION_REQUEST
                )
            }
        }

    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
    }

//    private fun sendData() {
//
//        val sharedPreference = SharedPreferencesHelper(this)
//
//        val originalDateStr = sharedPreference.getString("start_date")
//
//        val list = originalDateStr.split("-")
//        val filterDate = "${list[2]}/${list[1]}/${list[0]}"
//
//        val items = dbHelper.getAllItems()
//        val ids: ArrayList<String> = ArrayList()
//        for (item in items) {
//            ids.add(item._id)
//        }
//
//        var IDs: String = ids.joinToString(",")
//
//        if (IDs.isEmpty()) IDs = "0"
//
//        val sms = SmsReader.readNextPendingSms(this, filterDate, IDs)
//
//        if (sms?.let { Utils.messageType(it) } == MessageType.NONE) {
//            dbHelper.addItem(LocalItem(sms.id.toString(), "", sms.body))
//            return
//        }
//
//        if (sms != null) {
//            val amt = Utils.extractAmount(sms)
//            val ref = Utils.extractRef(sms)
//            val sender = Utils.extractSender(sms)
//            val transactionId = Utils.extractTransactionId(sms)
////            val type: String = if (Utils.messageType(sms) == MessageType.INCOME) "INCOME" else "EXPENSE"
//            val type = if (Utils.messageType(sms) == MessageType.INCOME) "INCOME" else if (Utils.messageType(sms) == MessageType.EXPENSE) "EXPENSE" else "NONE"
//
//            val postRequest = SmsRequest(
//                datetime = sms.apiDate,
//                message = sms.body,
//                ref = ref,
//                mref = "",
//                transactionId = transactionId,
//                sender = sender,
//                id = sms.id.toInt(),
//                type = type,
//                amount = amt,
//                includeInAccount = 1,
//                useTimes = "1"
//            )
//
////            Utils.sendPostRequest(postRequest,
////                success = { msg ->
////                    dbHelper.addItem(LocalItem(sms.id.toString(), "", sms.body))
////                    EventBus.getDefault().post(MessageSentEvent())
////                }, failure = { msg ->
////                })
//
//            Utils.sendTest(
//                success = { msg ->
//                  Log.d("TEST RESULTS", msg)
//                    EventBus.getDefault().post(MessageSentEvent())
//                }, failure = { msg ->
//                    Log.d("TEST FAILED", msg)
//                })
//        }
//
//
////        val postRequest = sms?.let {
//////            val type = Utils.messageType(it)
////            val amt = Utils.extractAmount(it)
////            val ref = Utils.extractRef(it)
////            val sender = Utils.extractSender(it)
////            val transactionId = Utils.extractTransactionId(it)
////
////            val type: String = if (Utils.messageType(it) == MessageType.INCOME) "INCOME" else "EXPENSE"
////
////            SmsRequest(
////                datetime = it.apiDate,
////                message = sms.body,
////                ref = ref,
////                mref = "",
////                transactionId = transactionId,
////                sender = sender,
////                id = sms.id.toInt(),
////                type = type,
////                amount = amt
////            )
////        }
//
//
////        if (postRequest != null) {
////            Utils.sendPostRequest(postRequest,
////                success = { msg ->
////                    dbHelper.addItem( LocalItem(item.id.toString(), mref, item.body))
////                }, failure = { msg ->
////
////                })
////        }
//    }
}