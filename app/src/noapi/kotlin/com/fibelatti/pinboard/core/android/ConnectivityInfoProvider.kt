package com.fibelatti.pinboard.core.android

import javax.inject.Inject

class ConnectivityInfoProvider @Inject constructor() {

    @Suppress("FunctionOnlyReturningConstant")
    fun isConnected(): Boolean = true
}
