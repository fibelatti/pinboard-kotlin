package com.fibelatti.pinboard.features

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.ScrollDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _currentScrollDirection: MutableStateFlow<ScrollDirection> = MutableStateFlow(ScrollDirection.IDLE)
    val currentScrollDirection: StateFlow<ScrollDirection> = _currentScrollDirection.asStateFlow()

    private val actionButtonClicks = MutableSharedFlow<Pair<String, Any?>>()
    private val menuItemClicks = MutableSharedFlow<Triple<String, MainState.MenuItemComponent, Any?>>()
    private val fabClicks = MutableSharedFlow<Pair<String, Any?>>()

    fun updateState(body: (MainState) -> MainState) {
        _state.update(body)
    }

    fun setCurrentScrollDirection(value: ScrollDirection) {
        _currentScrollDirection.value = value
    }

    fun actionButtonClicked(id: String, data: Any? = null) {
        launch {
            actionButtonClicks.emit(id to data)
        }
    }

    fun actionButtonClicks(id: String): Flow<Any?> = actionButtonClicks
        .filter { (eventId, _) -> eventId == id }
        .map { (_, data) -> data }

    fun menuItemClicked(id: String, menuItem: MainState.MenuItemComponent, data: Any? = null) {
        launch {
            menuItemClicks.emit(Triple(id, menuItem, data))
        }
    }

    fun menuItemClicks(id: String): Flow<Pair<MainState.MenuItemComponent, Any?>> = menuItemClicks
        .filter { (eventId, _, _) -> eventId == id }
        .map { (_, menuItem, data) -> menuItem to data }

    fun fabClicked(id: String, data: Any? = null) {
        launch {
            fabClicks.emit(id to data)
        }
    }

    fun fabClicks(id: String): Flow<Any?> = fabClicks
        .filter { (eventId, _) -> eventId == id }
        .map { (_, data) -> data }
}
