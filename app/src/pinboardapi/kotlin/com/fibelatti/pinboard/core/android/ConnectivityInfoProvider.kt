package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class ConnectivityInfoProvider @Inject constructor(
    private val connectivityManager: ConnectivityManager?
) {

    fun isConnected(): Boolean {
        return connectivityManager != null && connectivityManager.allNetworks
            .mapNotNull(connectivityManager::getNetworkCapabilities)
            .any { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
    }
}
