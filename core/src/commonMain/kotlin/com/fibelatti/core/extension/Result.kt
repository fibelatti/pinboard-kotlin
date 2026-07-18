package com.fibelatti.core.extension

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

public inline fun <T> Flow<Result<T>>.onEachSuccess(
    crossinline action: (value: T) -> Unit,
): Flow<Result<T>> {
    return this.onEach { result: Result<T> ->
        result.onSuccess(action)
    }
}

public inline fun <T> Flow<Result<T>>.onEachFailure(
    crossinline action: (exception: Throwable) -> Unit,
): Flow<Result<T>> {
    return this.onEach { result: Result<T> ->
        result.onFailure(action)
    }
}
