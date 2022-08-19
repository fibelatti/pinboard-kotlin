package com.fibelatti.pinboard.features.appstate

import kotlinx.coroutines.flow.Flow

interface AppStateRepository {

    val content: Flow<Content>

    suspend fun reset()

    suspend fun runAction(action: Action)
}
