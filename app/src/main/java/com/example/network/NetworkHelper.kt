package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager

class NetworkHelper(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    data class ConnectionInfo(
        val isConnected: Boolean,
        val isWifi: Boolean,
        val isMobile: Boolean,
        val networkName: String
    )

    fun getConnectionInfo(): ConnectionInfo {
        val cm = connectivityManager ?: return ConnectionInfo(false, false, false, "No Network")
        val activeNetwork = cm.activeNetwork ?: return ConnectionInfo(false, false, false, "Offline")
        val capabilities = cm.getNetworkCapabilities(activeNetwork)
            ?: return ConnectionInfo(false, false, false, "Disconnected")

        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isMobile = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        val name = when {
            isWifi -> {
                var ssid: String? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val wifiInfo = capabilities.transportInfo as? WifiInfo
                    ssid = wifiInfo?.ssid?.replace("\"", "")
                }
                if (ssid.isNullOrEmpty() || ssid == "<unknown ssid>") {
                    @Suppress("DEPRECATION")
                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    @Suppress("DEPRECATION")
                    ssid = wifiManager?.connectionInfo?.ssid?.replace("\"", "")
                }
                if (!ssid.isNullOrEmpty() && ssid != "<unknown ssid>") {
                    ssid
                } else {
                    "Wi-Fi Network"
                }
            }
            isMobile -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                val opName = tm?.networkOperatorName
                if (!opName.isNullOrEmpty()) {
                    "$opName Mobile"
                } else {
                    "Cellular Network"
                }
            }
            isEthernet -> "Ethernet"
            else -> "Connected"
        }

        return ConnectionInfo(
            isConnected = true,
            isWifi = isWifi,
            isMobile = isMobile,
            networkName = name
        )
    }
}
