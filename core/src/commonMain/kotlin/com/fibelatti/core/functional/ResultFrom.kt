package com.fibelatti.core.functional

public suspend fun <T> resultFrom(block: suspend () -> T): Result<T> = catching {
    retryIO {
        block()
    }
}
