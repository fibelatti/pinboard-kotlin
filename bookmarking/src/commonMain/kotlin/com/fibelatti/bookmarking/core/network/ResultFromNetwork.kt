package com.fibelatti.bookmarking.core.network

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.retryIO

public suspend fun <T> resultFromNetwork(block: suspend () -> T): Result<T> = catching {
    tooManyRequestsBackoff {
        retryIO {
            block()
        }
    }
}
