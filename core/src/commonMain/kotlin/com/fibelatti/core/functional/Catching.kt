package com.fibelatti.core.functional

import kotlin.Result
import kotlinx.coroutines.CancellationException

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was
 * successful, catching any [Throwable] exception that was thrown from the [block] function
 * execution and encapsulating it as a failure.
 *
 * Coroutine safe alternative to [runCatching] as it doesn't swallow [CancellationException].
 */
public inline fun <R> coRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (cancellationException: CancellationException) {
        throw cancellationException
    } catch (e: Throwable) {
        Result.failure(e)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated
 * value if `this` is a success, or the original encapsulated [Throwable] if it is a failure.
 *
 * Coroutine safe alternative to [Result.mapCatching] as it doesn't swallow [CancellationException].
 */
public inline fun <R, T> Result<T>.coMapCatching(transform: (value: T) -> R): Result<R> {
    return when (val exception: Throwable? = exceptionOrNull()) {
        null -> coRunCatching { transform(getOrThrow()) }
        else -> Result.failure(exception)
    }
}

/**
 * Returns the encapsulated result of the given [transform] function applied to the encapsulated
 * [Throwable] if `this` is a failure, or the original encapsulated value if it is a success.
 *
 * Coroutine safe alternative to [Result.recoverCatching] as it doesn't swallow
 * [CancellationException].
 */
public inline fun <R, T : R> Result<T>.coRecoverCatching(transform: (exception: Throwable) -> R): Result<R> {
    return when (val exception: Throwable? = exceptionOrNull()) {
        null -> this
        else -> coRunCatching { transform(exception) }
    }
}
