package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.fibelatti.core.platform.ConnectivityInfoProvider
import javax.inject.Inject

class AndroidConnectivityInfoProvider @Inject constructor(
    private val connectivityManager: ConnectivityManager?,
) : ConnectivityInfoProvider {

    override fun isConnected(): Boolean = connectivityManager
        ?.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}
