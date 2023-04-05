package com.fibelatti.pinboard.features.appstate

import kotlinx.coroutines.flow.StateFlow

interface AppStateRepository {

    val content: StateFlow<Content>

    suspend fun reset()

    suspend fun runAction(action: Action)
}
