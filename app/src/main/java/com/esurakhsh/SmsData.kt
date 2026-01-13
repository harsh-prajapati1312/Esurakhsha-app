package com.esurakhsh

data class SmsData(
    val address: String,
    val body: String,
    val date: Long,
    val type: Int,
    val id: String
)
