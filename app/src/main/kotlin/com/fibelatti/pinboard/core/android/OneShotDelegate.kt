package com.fibelatti.pinboard.core.android

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class OneShotDelegate {

    private var observer: LifecycleEventObserver? = null

    fun doOnLifecycleEvent(
        lifecycleOwner: LifecycleOwner,
        lifecycleEvent: Lifecycle.Event,
        body: () -> Unit,
    ) {
        observer = LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent) {
                observer?.let(lifecycleOwner.lifecycle::removeObserver)
                observer = null

                body()
            }
        }

        observer?.let(lifecycleOwner.lifecycle::addObserver)
    }
}
