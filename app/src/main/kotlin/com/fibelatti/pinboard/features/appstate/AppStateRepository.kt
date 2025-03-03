package com.fibelatti.pinboard.features.appstate

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

interface AppStateRepository {

    val appState: StateFlow<AppState>

    suspend fun runAction(action: Action)

    suspend fun runDelayedAction(action: Action, timeMillis: Long = 200L) {
        delay(timeMillis)
        runAction(action)
    }
}
