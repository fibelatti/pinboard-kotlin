package com.fibelatti.pinboard.core.android.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fibelatti.pinboard.features.appstate.Action
import com.fibelatti.pinboard.features.appstate.AppState
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

abstract class BaseViewModel(
    dispatcher: CoroutineDispatcher,
    private val appStateRepository: AppStateRepository,
) : ViewModel() {

    protected val scope: CoroutineScope = viewModelScope + dispatcher

    val appState: StateFlow<AppState> get() = appStateRepository.appState

    val error: StateFlow<Throwable?> get() = _error.asStateFlow()
    private val _error = MutableStateFlow<Throwable?>(null)

    protected inline fun <reified T : Content> filteredContent(): Flow<T> = appState.mapNotNull { it.content as? T }

    fun runAction(action: Action) {
        scope.launch(Dispatchers.Main.immediate) {
            appStateRepository.runAction(action)
        }
    }

    fun runDelayedAction(action: Action, timeMillis: Long = 200L) {
        scope.launch {
            appStateRepository.runDelayedAction(action, timeMillis)
        }
    }

    protected fun handleError(error: Throwable) {
        _error.value = error
    }

    fun errorHandled() {
        _error.value = null
    }
}
