package com.fibelatti.pinboard.core.network

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject
import javax.inject.Singleton

// Pinboard API requires a minimum of 3 seconds between each request
private const val API_THROTTLE_TIME = 3000L

interface RateLimitRunner {

    suspend fun <T> run(body: suspend () -> T): T
}

/**
 * A suspend function runner that complies with Pinboard API rate limits.
 *
 * If no calls were made within the last [API_THROTTLE_TIME] milliseconds then it makes the call
 * straight away, otherwise it waits until a new call can be made. Requests will be queued and made
 * one at a time.
 */
@Singleton
class ApiRateLimitRunner @Inject constructor() : RateLimitRunner {

    private val mutex = Mutex()

    /**
     * Calls [body] applying this runner policies
     *
     * @return [T] returned by [body] without any changes
     */
    override suspend fun <T> run(body: suspend () -> T): T {
        mutex.lock()
        scheduleUnlock()
        return body()
    }

    /**
     * Unlocks [mutex] after delaying for [API_THROTTLE_TIME].
     *
     * Launched on [GlobalScope] specifically so it doesn't block its parent coroutine.
     */
    private suspend fun scheduleUnlock() {
        GlobalScope.launch {
            delay(API_THROTTLE_TIME)
            mutex.unlock()
        }
    }
}
