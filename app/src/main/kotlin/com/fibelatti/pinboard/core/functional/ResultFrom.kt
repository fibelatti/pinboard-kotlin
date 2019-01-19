package com.fibelatti.pinboard.core.functional

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.retryIO

suspend inline fun <T> resultFrom(crossinline block: suspend () -> T): Result<T> =
    catching { retryIO { block() } }
