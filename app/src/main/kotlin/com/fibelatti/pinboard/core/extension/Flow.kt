package com.fibelatti.pinboard.core.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn

fun <T> Flow<T>.launchInAndFlowWith(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): Job = flowWithLifecycle(
    lifecycle = lifecycleOwner.lifecycle,
    minActiveState = minActiveState,
).launchIn(
    scope = lifecycleOwner.lifecycleScope,
)
