package com.presetschool.momobookapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.presetschool.momobookapp.databinding.ActivityMainBinding
import com.presetschool.momobookapp.model.LocalItem
import com.presetschool.momobookapp.model.MessageSentEvent
import com.presetschool.momobookapp.model.MessageType
import com.presetschool.momobookapp.model.SmsRequest
import com.presetschool.momobookapp.service.DatabaseHelper
import com.presetschool.momobookapp.service.ForegroundService
import com.presetschool.momobookapp.service.SharedPreferencesHelper
import com.presetschool.momobookapp.service.SmsReader
import com.presetschool.momobookapp.service.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.LocalDate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private val SMS_PERMISSION_REQUEST = 101
    private lateinit var dbHelper: DatabaseHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val sharedPreference = SharedPreferencesHelper(this.baseContext)
//         dbHelper = DatabaseHelper(this)
////        dbHelper.deleteAllItems()
//
////        dbHelper.addItem(LocalItem())
//
//
//
//        val startDate = sharedPreference.getString("start_date")
//        if (startDate.isEmpty()) {
//            sharedPreference.saveString("start_date", LocalDate.now().toString())
//        }
//
//        val delayTime: Int = sharedPreference.getInt("delay_time", 0)
//        if (delayTime == 0) {
//            sharedPreference.saveInt("delay_time", 5)
//        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


//        startService(Intent(this, ForegroundService::class.java))
//        startOverlayService()
//
//        checkAndRequestPermissions()

        // Initialize overlay permission launcher
//        overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (Settings.canDrawOverlays(this)) {
//                startService(Intent(this, ForegroundService::class.java))
//                startOverlayService()
////                Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
//                checkAndRequestPermissions()
//            } else {
////                Toast.makeText(this, "Overlay permission denied!", Toast.LENGTH_SHORT).show()
//                checkAndRequestPermissions()
//            }
//        }

//        if (!Settings.canDrawOverlays(this)) {
////            requestOverlayPermission()
//        } else {
//            startService(Intent(this, ForegroundService::class.java))
//            checkAndRequestPermissions()
//        }

//        val timer = Timer()
//        timer.schedule(object : TimerTask() {
//            override fun run() {
//                EventBus.getDefault().post(MessageSentEvent())
//            }
//        }, 5000)



//        CoroutineScope(Dispatchers.Main).launch {
//            while (isActive) {
//               val time = sharedPreference.getInt("delay_time").toLong()
////               sendData()
////                delay(1000 * 60 * time)
//                delay(1000  * time)
//            }
//        }


    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
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
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
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
    }


}