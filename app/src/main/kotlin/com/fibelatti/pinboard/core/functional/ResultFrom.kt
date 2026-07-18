package com.fibelatti.pinboard.core.functional

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.retry

suspend fun <T> resultFrom(block: suspend () -> T): Result<T> = catching {
    retry(block = block)
}
