package com.fibelatti.pinboard.core.android

import dagger.hilt.android.lifecycle.RetainedLifecycle
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class RetainedLifecycleScope(context: CoroutineContext) : CoroutineScope, RetainedLifecycle.OnClearedListener {

    override val coroutineContext: CoroutineContext = context

    override fun onCleared() {
        coroutineContext.cancel()
    }
}
