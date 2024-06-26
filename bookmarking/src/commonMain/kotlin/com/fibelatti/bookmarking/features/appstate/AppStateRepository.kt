package com.fibelatti.bookmarking.features.appstate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

public interface AppStateRepository {

    public val content: StateFlow<Content>

    public suspend fun reset()

    public suspend fun runAction(action: Action)

    public suspend fun runDelayedAction(action: Action, timeMillis: Long = 200L) {
        delay(timeMillis)
        runAction(action)
    }
}
