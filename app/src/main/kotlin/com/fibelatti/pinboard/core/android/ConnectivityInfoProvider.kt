package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.fibelatti.pinboard.core.di.MainVariant
import javax.inject.Inject

class ConnectivityInfoProvider @Inject constructor(
    private val connectivityManager: ConnectivityManager?,
    @MainVariant private val mainVariant: Boolean,
) {

    fun isConnected(): Boolean = if (mainVariant) {
        connectivityManager != null && connectivityManager.allNetworks
            .mapNotNull(connectivityManager::getNetworkCapabilities)
            .any { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
    } else {
        true
    }
}
