package com.fibelatti.pinboard

import app.cash.turbine.Event
import app.cash.turbine.TurbineTestContext

/**
 * Shorthand function to retrieve the list of items that have been received by the flow under test.
 *
 * @see TurbineTestContext.cancelAndConsumeRemainingEvents
 */
suspend fun <T> TurbineTestContext<T>.receivedItems(): List<T> {
    return cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<T>>().map { it.value }
}
