package com.fibelatti.pinboard.features.appstate

import kotlinx.coroutines.flow.Flow

interface AppStateRepository {

    fun getContent(): Flow<Content>

    fun reset()

    suspend fun runAction(action: Action)
}
