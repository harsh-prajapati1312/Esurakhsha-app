package com.esurakhsh

import android.content.Context
import android.database.Cursor
import android.provider.Telephony
import android.util.Log

class SmsRepository(private val context: Context) {

    fun getAllSms(): List<SmsData> {
        val smsList = mutableListOf<SmsData>()
        try {
            val uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE
            )
            val sortOrder = "${Telephony.Sms.DATE} DESC"

            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val indexId = it.getColumnIndex(Telephony.Sms._ID)
                val indexAddress = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val indexBody = it.getColumnIndex(Telephony.Sms.BODY)
                val indexDate = it.getColumnIndex(Telephony.Sms.DATE)
                val indexType = it.getColumnIndex(Telephony.Sms.TYPE)

                while (it.moveToNext()) {
                    val id = if (indexId != -1) it.getString(indexId) else ""
                    val address = if (indexAddress != -1) it.getString(indexAddress) ?: "Unknown" else "Unknown"
                    val body = if (indexBody != -1) it.getString(indexBody) ?: "" else ""
                    val date = if (indexDate != -1) it.getLong(indexDate) else 0L
                    val type = if (indexType != -1) it.getInt(indexType) else 0

                    smsList.add(SmsData(address, body, date, type, id))
                }
            }
        } catch (e: Exception) {
            Log.e("SmsRepository", "Error reading SMS", e)
        }
        return smsList
    }
}
