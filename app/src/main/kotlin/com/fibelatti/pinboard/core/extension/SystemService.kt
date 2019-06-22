package com.fibelatti.pinboard.core.extension

import android.net.ConnectivityManager
import com.fibelatti.core.extension.orFalse

fun ConnectivityManager?.isConnected(): Boolean = this?.activeNetworkInfo?.isConnected.orFalse()
