package com.esurakhsh

import android.accounts.AccountManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import java.net.Inet4Address

class DeviceInfoRepository(private val context: Context) {

    fun getInstalledApps(): List<AppData> {
        val apps = mutableListOf<AppData>()
        val pm = context.packageManager
        // Get all installed packages
        val packages = pm.getInstalledPackages(0)
        
        for (pkg in packages) {
            val appName = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
            val packageName = pkg.packageName
            val versionName = pkg.versionName ?: "Unknown"
            apps.add(AppData(appName, packageName, versionName))
        }
        return apps
    }

    fun getGoogleAccounts(): List<String> {
        val emails = mutableListOf<String>()
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            for (account in accounts) {
                emails.add(account.name)
            }
        } catch (e: Exception) {
            // Permission GET_ACCOUNTS might be needed or denied
            emails.add("Error or Permission Denied: ${e.message}")
        }
        
        // DEMO MODE: If no accounts found (common in emulators), add fake ones
        if (emails.isEmpty()) {
            emails.add("demo.target.user@gmail.com (Demo)")
            emails.add("investigation.case.001@gmail.com (Demo)")
        }
        
        return emails
    }

    fun getIpAddress(): String {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork)
            if (linkProperties != null) {
                for (linkAddress in linkProperties.linkAddresses) {
                    val address = linkAddress.address
                    if (address is Inet4Address) {
                        return address.hostAddress ?: "Unavailable"
                    }
                }
            }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
        return "Unavailable"
    }
}
