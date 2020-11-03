package com.fibelatti.pinboard.core.android.base

import androidx.annotation.CallSuper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * [ViewModel] that also implements [CoroutineScope], allowing coroutines to be launched from it.
 *
 * All coroutine jobs will be cancelled when [onCleared] is called.
 */
abstract class BaseViewModel : ViewModel(), CoroutineScope {

    /**
     * A [SupervisorJob] that will be the parent of any coroutines launched by inheritors of this [ViewModel].
     */
    private val parentJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + parentJob

    /**
     * A [Flow] of [Throwable] that will hold any error state that happened in this [ViewModel].
     */
    val error: Flow<Throwable> get() = _error.filterNotNull()
    private val _error = MutableStateFlow<Throwable?>(null)

    /**
     * Cancels the [parentJob].
     */
    @CallSuper
    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

    /**
     * Calls [MutableLiveData.postValue] on [_error] with the received error as its argument.
     */
    protected fun handleError(error: Throwable) {
        _error.value = error
    }
}
