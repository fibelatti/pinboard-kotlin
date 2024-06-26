package com.fibelatti.bookmarking.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [ViewModel] that also implements [CoroutineScope], allowing coroutines to be launched from it.
 *
 * All coroutine jobs will be cancelled when [onCleared] is called.
 */
public abstract class BaseViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext

    /**
     * A [Flow] of [Throwable] that will hold the latest error that happened in this [ViewModel]
     * until [errorHandled] is called.
     */
    public val error: StateFlow<Throwable?> get() = _error.asStateFlow()
    private val _error = MutableStateFlow<Throwable?>(null)

    protected fun handleError(error: Throwable) {
        _error.value = error
    }

    public fun errorHandled() {
        _error.value = null
    }
}
