package com.fibelatti.pinboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Function to assist testing [Flow]s (and mainly [StateFlow]s) that should receive no values.
 *
 * The test would typically hang as the receiver [Flow] has no termination signal, so this function wraps the collection
 * of the [firstOrNull] from the receiver with a timeout that is short enough to avoid big impacts in the execution of
 * the test suite, and when used in conjunction with [runUnconfinedTest] can assist ensuring that no values were collected.
 *
 * @receiver the [Flow] under test
 * @return true if [firstOrNull] returned null or if [withTimeoutOrNull] timed out, false otherwise
 */
suspend fun <T> Flow<T>.isEmpty(): Boolean = withTimeoutOrNull(timeMillis = 50L) { firstOrNull() } == null

/**
 * Function to assist testing a hot [Flow] (mainly [SharedFlow] with replay=0). Call this function before emitting
 * anything to the receiver [Flow]. On call, a new coroutine is launched to collect its values which are then
 * returned as a [List]. The job from the launched coroutine will be cancelled after the [autoCancellationDelayMillis]
 * to prevent that test hangs since the receiver has no termination signal.
 *
 * @receiver the [Flow] under test
 * @return a [List] with the collected values
 */
suspend fun <T> Flow<T>.collectIn(scope: CoroutineScope, autoCancellationDelayMillis: Long = 50L): List<T> {
    val result = mutableListOf<T>()
    val job = scope.launch {
        toList(result)
    }

    scope.launch {
        delay(autoCancellationDelayMillis)
        job.cancel()
    }

    return result
}

/**
 * Shorthand function to [runTest] with an [UnconfinedTestDispatcher] as its context, mainly used to test hot flows.
 */
fun runUnconfinedTest(testBody: suspend TestScope.() -> Unit) = runTest(
    context = UnconfinedTestDispatcher(),
    testBody = testBody,
)
