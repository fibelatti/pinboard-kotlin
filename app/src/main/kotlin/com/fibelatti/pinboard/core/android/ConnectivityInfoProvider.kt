package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.koin.core.annotation.Single

@Single
class ConnectivityInfoProvider(
    private val connectivityManager: ConnectivityManager?,
) {

    fun isConnected(): Boolean = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}
