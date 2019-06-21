package com.fibelatti.pinboard.core.functional

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SingleRunner @Inject constructor() {

    private val mutex = Mutex()

    suspend fun <T> afterPrevious(block: suspend () -> T): T = mutex.withLock { block() }
}
