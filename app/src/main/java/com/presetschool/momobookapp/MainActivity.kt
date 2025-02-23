package com.presetschool.momobookapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.presetschool.momobookapp.databinding.ActivityMainBinding
import com.presetschool.momobookapp.service.ForegroundService
import android.Manifest
import android.util.Log
import com.presetschool.momobookapp.service.SmsReader
import com.presetschool.momobookapp.service.Utils


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>
    private val SMS_PERMISSION_REQUEST = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


        // Initialize overlay permission launcher
        overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, ForegroundService::class.java))
                startOverlayService()
                Toast.makeText(this, "Overlay permission granted!", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "Overlay permission denied!", Toast.LENGTH_SHORT).show()
                checkAndRequestPermissions()
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            startService(Intent(this, ForegroundService::class.java))
            checkAndRequestPermissions()
        }

        readMessages();

    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST
            )
        } else {
            Toast.makeText(this, "SMS Permission Already Granted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun readMessages(){
        val filterDate = "01/02/2025" // 🟢 Change this to the desired date (dd/MM/yyyy)
        val smsList = SmsReader.readSms(this, filterDate)

        for ( m in smsList){
            Log.d(Utils.messageType(m).toString(), m.body)
        }

//        val messages = if (smsList.isNotEmpty()) {
//            smsList.joinToString("\n\n")
//        } else {
//            "No SMS messages from VANY after $filterDate."
//        }

//        Log.d("messages", messages)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }



}