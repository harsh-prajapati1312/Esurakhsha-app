package com.esurakhsh

import android.content.Context
import android.provider.CallLog
import android.util.Log

class CallLogRepository(private val context: Context) {

    fun getAllCallLogs(): List<CallLogData> {
        val callLogList = mutableListOf<CallLogData>()
        try {
            val uri = CallLog.Calls.CONTENT_URI
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
            )
            val sortOrder = "${CallLog.Calls.DATE} DESC"

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val indexType = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val indexDate = cursor.getColumnIndex(CallLog.Calls.DATE)
                val indexDuration = cursor.getColumnIndex(CallLog.Calls.DURATION)

                while (cursor.moveToNext()) {
                    val number = if (indexNumber != -1) cursor.getString(indexNumber) ?: "Unknown" else "Unknown"
                    val type = if (indexType != -1) cursor.getInt(indexType) else 0
                    val date = if (indexDate != -1) cursor.getLong(indexDate) else 0L
                    val duration = if (indexDuration != -1) cursor.getLong(indexDuration) else 0L

                    callLogList.add(CallLogData(number, type, date, duration))
                }
            }
        } catch (e: SecurityException) {
            Log.e("CallLogRepository", "Permission denied", e)
        } catch (e: Exception) {
            Log.e("CallLogRepository", "Error reading call logs", e)
        }
        return callLogList
    }
}
