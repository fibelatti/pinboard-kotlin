package com.fibelatti.pinboard.features.appstate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

interface AppStateRepository {

    val content: StateFlow<Content>

    suspend fun reset()

    suspend fun runAction(action: Action)

    suspend fun runDelayedAction(action: Action, timeMillis: Long = 200L) {
        delay(timeMillis)
        runAction(action)
    }
}
