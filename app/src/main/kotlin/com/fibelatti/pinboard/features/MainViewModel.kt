package com.fibelatti.pinboard.features

import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.Reset
import com.fibelatti.pinboard.features.main.reducer.MainStateReducer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
    mainStateReducers: Map<Class<out Content>, @JvmSuppressWildcards MainStateReducer>,
) : BaseViewModel(scope, appStateRepository) {

    private val reducer: MutableSharedFlow<suspend (MainState) -> MainState> = MutableSharedFlow()

    val state: StateFlow<MainState> = reducer
        .scan(initial = MainState()) { state, reducer -> reducer(state) }
        .stateIn(scope = scope, started = sharingStarted, initialValue = MainState())

    private val actionButtonClicks = MutableSharedFlow<Pair<ContentType, Any?>>()
    private val menuItemClicks = MutableSharedFlow<Triple<ContentType, MainState.MenuItemComponent, Any?>>()
    private val fabClicks = MutableSharedFlow<Pair<ContentType, Any?>>()

    init {
        appStateRepository.appState
            .onEach { appState ->
                mainStateReducers[appState.content::class.java]?.let { mainStateReducer: MainStateReducer ->
                    reducer.emit { current: MainState -> mainStateReducer(current, appState) }
                }
            }
            .launchIn(scope)
    }

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

    fun actionButtonClicked(contentType: ContentType, data: Any? = null) {
        scope.launch {
            actionButtonClicks.emit(contentType to data)
        }
    }

    fun actionButtonClicks(contentType: ContentType): Flow<Any?> = actionButtonClicks
        .filter { (type, _) -> type == contentType }
        .map { (_, data) -> data }

    fun menuItemClicked(contentType: ContentType, menuItem: MainState.MenuItemComponent, data: Any? = null) {
        scope.launch {
            menuItemClicks.emit(Triple(contentType, menuItem, data))
        }
    }

    fun menuItemClicks(contentType: ContentType): Flow<Pair<MainState.MenuItemComponent, Any?>> = menuItemClicks
        .filter { (type, _, _) -> type == contentType }
        .map { (_, menuItem, data) -> menuItem to data }

    fun fabClicked(contentType: ContentType, data: Any? = null) {
        scope.launch {
            fabClicks.emit(contentType to data)
        }
    }

    fun fabClicks(contentType: ContentType): Flow<Any?> = fabClicks
        .filter { (type, _) -> type == contentType }
        .map { (_, data) -> data }
}
