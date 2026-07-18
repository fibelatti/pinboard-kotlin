package com.fibelatti.core.functional

import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.io.IOException

/**
 * Shorthand function to retry executing the [block] automatically for N [times] in case a retryable
 * type of [Exception] happens.
 *
 * @param times how many times [block] will attempt to execute, default is 5
 * @param initialDelay how long the execution will be suspended for before trying for the first
 * time, in milliseconds, default is 100
 * @param maxDelay max amount of time waiting before retrying, in milliseconds, default is 1000
 * @param factor multiplying factor of delay, default is 2
 * @param retryableTypes [Set] of [Exception] types that allow retry, subtypes included. Default to
 * [IOException]
 * @param block code block to be executed inside the try catch
 *
 * @return [T] if successful, IOException if all retries failed
 */
public suspend fun <T> retry(
    times: Int = 5,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    retryableTypes: Set<KClass<out Exception>> = setOf(IOException::class),
    block: suspend () -> T,
): T {
    var currentDelay: Long = initialDelay

    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            if (e is CancellationException || retryableTypes.none { type -> type.isInstance(e) }) throw e
        }

        delay(timeMillis = currentDelay)

        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    return block()
}
