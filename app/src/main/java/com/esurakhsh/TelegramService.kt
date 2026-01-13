package com.esurakhsh

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class TelegramService(private val botToken: String, private val chatId: String) {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendText(text: String) {
        val url = "https://api.telegram.org/bot$botToken/sendMessage"
        
        val json = gson.toJson(mapOf("chat_id" to chatId, "text" to text))
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("TelegramService", "Failed to send text: ${response.code}")
                }
            }
        } catch (e: IOException) {
            Log.e("TelegramService", "Error sending text", e)
        }
    }

    fun sendDocument(file: File, caption: String = "") {
        val url = "https://api.telegram.org/bot$botToken/sendDocument"

        val fileBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart("caption", caption)
            .addFormDataPart("document", file.name, fileBody)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                 if (!response.isSuccessful) {
                    Log.e("TelegramService", "Failed to send document: ${response.code} ${response.body?.string()}")
                }
            }
        } catch (e: IOException) {
            Log.e("TelegramService", "Error sending document", e)
        }
    }
}
