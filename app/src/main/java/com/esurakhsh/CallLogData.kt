package com.esurakhsh

data class CallLogData(
    val number: String,
    val type: Int, // CallLog.Calls.INCOMING_TYPE, etc.
    val date: Long,
    val duration: Long
)
