package com.esurakhsh

data class AppData(
    val appName: String,
    val packageName: String,
    val versionName: String
)

data class DeviceInfo(
    val ipAddress: String,
    val googleAccounts: List<String>
)
