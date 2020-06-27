package com.fibelatti.pinboard.core.network

import kotlinx.coroutines.delay
import retrofit2.HttpException

/**
 * Shorthand function to retry executing [block] automatically for [times] in case an [HttpException]
 * with code 429 (Too many requests) happens.
 *
 * @param times how many times [block] will attempt to execute, default is 3
 * @param initialDelay how long the execution will be suspended for before trying for the first time,
 * in milliseconds, default is 500
 * @param maxDelay max amount of time waiting before retrying, in milliseconds, default is 2000
 * @param factor multiplying factor of delay, default is 2
 * @param block code block to be executed inside the try catch
 *
 * @return [T] if successful, HttpException if all retries failed
 */
@Suppress("MagicNumber")
suspend fun <T> tooManyRequestsBackoff(
    times: Int = 3,
    initialDelay: Long = 500,
    maxDelay: Long = 2000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay

    repeat(times - 1) {
        try {
            return block()
        } catch (httpException: HttpException) {
            if (httpException.code() == 429) {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            } else {
                throw httpException
            }
        }
    }

    return block()
}
