package com.fibelatti.pinboard.core.functional

import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.core.functional.retry

suspend fun <T> resultFrom(block: suspend () -> T): Result<T> = coRunCatching {
    retry(block = block)
}
