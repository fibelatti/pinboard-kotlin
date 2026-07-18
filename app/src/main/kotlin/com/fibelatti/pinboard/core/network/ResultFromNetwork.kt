package com.fibelatti.pinboard.core.network

import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.core.functional.retry

suspend fun <T> resultFromNetwork(block: suspend () -> T): Result<T> = coRunCatching {
    tooManyRequestsBackoff {
        retry(block = block)
    }
}
