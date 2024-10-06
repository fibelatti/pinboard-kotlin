package com.fibelatti.core.functional

import kotlinx.coroutines.delay
import kotlinx.io.IOException

/**
 * Shorthand function to retry executing [block] automatically for [times] in case an [IOException] happens.
 *
 * @param times how many times [block] will attempt to execute, default is 5
 * @param initialDelay how long the execution will be suspended for before trying for the first time,
 * in milliseconds, default is 100
 * @param maxDelay max amount of time waiting before retrying, in milliseconds, default is 1000
 * @param factor multiplying factor of delay, default is 2
 * @param block code block to be executed inside the try catch
 *
 * @return [T] if successful, IOException if all retries failed
 */
public suspend fun <T> retryIO(
    times: Int = 5,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T,
): T {
    var currentDelay = initialDelay

    repeat(times - 1) {
        try {
            return block()
        } catch (ignored: IOException) {
        }

        delay(currentDelay)

        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    return block()
}
