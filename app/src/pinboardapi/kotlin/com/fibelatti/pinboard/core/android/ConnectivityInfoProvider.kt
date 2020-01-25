package com.fibelatti.pinboard.core.android

import android.net.ConnectivityManager
import com.fibelatti.core.extension.orFalse
import javax.inject.Inject

class ConnectivityInfoProvider @Inject constructor(private val connectivityManager: ConnectivityManager?) {

    fun isConnected(): Boolean = connectivityManager?.activeNetworkInfo?.isConnected.orFalse()
}
