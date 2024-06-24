package com.fibelatti.bookmarking.core.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.koin.core.annotation.Single

@Single
public actual class ConnectivityInfoProvider(
    private val connectivityManager: ConnectivityManager?,
) {

    public actual fun isConnected(): Boolean = connectivityManager
        ?.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}
