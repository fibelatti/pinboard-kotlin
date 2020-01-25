package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityInfoProvider @Inject constructor(connectivityManager: ConnectivityManager?) {

    var hasConnection: Boolean = false

    init {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()

        connectivityManager?.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    hasConnection = true
                }

                override fun onLost(network: Network) {
                    hasConnection = false
                }
            }
        )
    }

    fun isConnected(): Boolean = hasConnection
}
