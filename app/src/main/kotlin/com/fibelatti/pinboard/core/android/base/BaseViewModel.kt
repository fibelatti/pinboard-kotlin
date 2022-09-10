package com.fibelatti.pinboard.core.android.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlin.coroutines.CoroutineContext

/**
 * [ViewModel] that also implements [CoroutineScope], allowing coroutines to be launched from it.
 *
 * All coroutine jobs will be cancelled when [onCleared] is called.
 */
abstract class BaseViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext

    /**
     * A [Flow] of [Throwable] that will hold any error state that happened in this [ViewModel].
     */
    val error: Flow<Throwable> get() = _error.filterNotNull()
    private val _error = MutableStateFlow<Throwable?>(null)

    /**
     * Calls [MutableLiveData.postValue] on [_error] with the received error as its argument.
     */
    protected fun handleError(error: Throwable) {
        _error.value = error
    }
}
