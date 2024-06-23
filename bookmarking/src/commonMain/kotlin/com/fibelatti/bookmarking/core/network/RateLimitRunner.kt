package com.fibelatti.bookmarking.core.network

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

public interface RateLimitRunner {

    public suspend fun <T> run(body: suspend () -> T): T

    public suspend fun <T> run(throttleTime: Long, body: suspend () -> T): T
}

/**
 * A suspend function runner that complies with Pinboard API rate limits.
 *
 * If no calls were made within the last [throttleTime] milliseconds then it makes the call
 * straight away, otherwise it waits until a new call can be made. Requests will be queued and made
 * one at a time.
 */
@Single
internal class ApiRateLimitRunner(private val throttleTime: Long) : RateLimitRunner {

    private val mutex = Mutex()

    /**
     * Calls [body] applying this runner [throttleTime]
     *
     * @return [T] returned by [body] without any changes
     */
    override suspend fun <T> run(body: suspend () -> T): T = run(throttleTime, body)

    /**
     * Calls [body] applying the given [throttleTime]
     *
     * @return [T] returned by [body] without any changes
     */
    override suspend fun <T> run(throttleTime: Long, body: suspend () -> T): T {
        mutex.lock()
        scheduleUnlock(throttleTime)
        return body()
    }

    /**
     * Unlocks [mutex] after delaying for [throttleTime].
     *
     * Launched on a [NonCancellable] context specifically so it doesn't block its parent coroutine.
     */
    private suspend fun scheduleUnlock(throttleTime: Long) {
        withContext(NonCancellable) {
            launch {
                delay(throttleTime)
                mutex.unlock()
            }
        }
    }
}
