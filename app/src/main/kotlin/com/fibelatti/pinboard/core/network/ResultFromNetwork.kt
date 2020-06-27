package com.fibelatti.pinboard.core.network

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.retryIO

suspend fun <T> resultFromNetwork(block: suspend () -> T): Result<T> = catching {
    tooManyRequestsBackoff {
        retryIO {
            block()
        }
    }
}
