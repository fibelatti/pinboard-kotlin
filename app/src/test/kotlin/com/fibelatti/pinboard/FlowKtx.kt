package com.fibelatti.pinboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Function to assist testing [Flow]s (and mainly [StateFlow]s) that should receive no values.
 *
 * The test would typically hang as the receiver [Flow] has no termination signal, so this function wraps the collection
 * of the [firstOrNull] from the receiver with a timeout that is short enough to avoid big impacts in the execution of
 * the test suite, and when used in conjunction with [runBlocking] can assist ensuring that no values were collected.
 *
 * @receiver the [Flow] under test
 * @return true if [firstOrNull] returned null or if [withTimeoutOrNull] timed out, false otherwise
 */
suspend fun <T> Flow<T>.isEmpty(): Boolean = withTimeoutOrNull(timeMillis = 50L) { firstOrNull() } == null
