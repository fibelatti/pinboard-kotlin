package com.fibelatti.pinboard.features

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.Reset
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
) : BaseViewModel(scope, appStateRepository) {

    private val reducer: MutableSharedFlow<suspend (MainState) -> MainState> = MutableSharedFlow()

    val state: StateFlow<MainState> = reducer
        .scan(initial = MainState()) { state, reducer -> reducer(state) }
        .stateIn(scope = scope, started = sharingStarted, initialValue = MainState())

    private val actionButtonClicks = MutableSharedFlow<Pair<String, Any?>>()
    private val menuItemClicks = MutableSharedFlow<Triple<String, MainState.MenuItemComponent, Any?>>()
    private val fabClicks = MutableSharedFlow<Pair<String, Any?>>()

    fun updateState(body: (MainState) -> MainState) {
        scope.launch(Dispatchers.Main.immediate) {
            reducer.emit(body)
        }
    }

    fun setMultiPanelAvailable(value: Boolean) {
        runAction(MultiPanelAvailabilityChanged(available = value))
    }

    fun setCurrentScrollDirection(value: ScrollDirection) {
        scope.launch(Dispatchers.Main.immediate) {
            reducer.emit { current -> current.copy(scrollDirection = value) }
        }
    }

    fun navigateBack() {
        runAction(NavigateBack)
    }

    fun resetAppNavigation() {
        runAction(Reset)
    }

    fun actionButtonClicked(id: String, data: Any? = null) {
        scope.launch {
            actionButtonClicks.emit(id to data)
        }
    }

    fun actionButtonClicks(id: String): Flow<Any?> = actionButtonClicks
        .filter { (eventId, _) -> eventId == id }
        .map { (_, data) -> data }

    fun menuItemClicked(id: String, menuItem: MainState.MenuItemComponent, data: Any? = null) {
        scope.launch {
            menuItemClicks.emit(Triple(id, menuItem, data))
        }
    }

    fun menuItemClicks(id: String): Flow<Pair<MainState.MenuItemComponent, Any?>> = menuItemClicks
        .filter { (eventId, _, _) -> eventId == id }
        .map { (_, menuItem, data) -> menuItem to data }

    fun fabClicked(id: String, data: Any? = null) {
        scope.launch {
            fabClicks.emit(id to data)
        }
    }

    fun fabClicks(id: String): Flow<Any?> = fabClicks
        .filter { (eventId, _) -> eventId == id }
        .map { (_, data) -> data }
}
