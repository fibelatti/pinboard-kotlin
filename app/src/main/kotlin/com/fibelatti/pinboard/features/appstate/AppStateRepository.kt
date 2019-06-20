package com.fibelatti.pinboard.features.appstate

import androidx.lifecycle.LiveData

interface AppStateRepository {

    fun getContent(): LiveData<Content>

    suspend fun runAction(action: Action)
}
