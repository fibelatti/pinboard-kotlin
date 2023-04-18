package com.fibelatti.pinboard.features

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : BaseViewModel() {

    private val _state: MutableStateFlow<MainState> = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    private val _navigationClicks: MutableSharedFlow<String> = MutableSharedFlow()
    private val _actionButtonClicks: MutableSharedFlow<Pair<String, Any?>> = MutableSharedFlow()
    private val _menuItemClicks: MutableSharedFlow<Triple<String, Int, Any?>> = MutableSharedFlow()
    private val _fabClicks: MutableSharedFlow<Pair<String, Any?>> = MutableSharedFlow()

    fun updateState(body: (MainState) -> MainState) {
        _state.update(body)
    }

    fun navigationClicked(id: String) {
        launch {
            _navigationClicks.emit(id)
        }
    }

    fun navigationClicks(id: String): Flow<String> = _navigationClicks
        .filter { _id -> _id == id }

    fun actionButtonClicked(id: String, data: Any? = null) {
        launch {
            _actionButtonClicks.emit(id to data)
        }
    }

    fun actionButtonClicks(id: String): Flow<Any?> = _actionButtonClicks
        .filter { (_id, _) -> _id == id }
        .map { (_, data) -> data }

    fun menuItemClicked(id: String, @IdRes menuItemId: Int, data: Any? = null) {
        launch {
            _menuItemClicks.emit(Triple(id, menuItemId, data))
        }
    }

    fun menuItemClicks(id: String): Flow<Pair<Int, Any?>> = _menuItemClicks
        .filter { (_id, _, _) -> _id == id }
        .map { (_, menuItemId, data) -> menuItemId to data }

    fun fabClicked(id: String, data: Any? = null) {
        launch {
            _fabClicks.emit(id to data)
        }
    }

    fun fabClicks(id: String): Flow<Any?> = _fabClicks
        .filter { (_id, _) -> _id == id }
        .map { (_, data) -> data }
}

data class MainState(
    val title: TitleComponent = TitleComponent.Gone,
    val subtitle: TitleComponent = TitleComponent.Gone,
    val navigation: NavigationComponent = NavigationComponent.Gone,
    val actionButton: ActionButtonComponent = ActionButtonComponent.Gone,
    val bottomAppBar: BottomAppBarComponent = BottomAppBarComponent.Gone,
    val floatingActionButton: FabComponent = FabComponent.Gone,
) {

    sealed class TitleComponent {

        object Gone : TitleComponent()
        data class Visible(val label: String) : TitleComponent()
    }

    sealed class NavigationComponent {

        abstract val id: String

        object Gone : NavigationComponent() {

            override val id: String = UUID.randomUUID().toString()
        }

        data class Visible(
            override val id: String,
            @DrawableRes val icon: Int = R.drawable.ic_back_arrow,
        ) : NavigationComponent()
    }

    sealed class ActionButtonComponent {

        abstract val id: String

        object Gone : ActionButtonComponent() {

            override val id: String = UUID.randomUUID().toString()
        }

        data class Visible(
            override val id: String,
            val label: String,
            val data: Any? = null,
        ) : ActionButtonComponent()
    }

    sealed class BottomAppBarComponent {

        abstract val id: String

        object Gone : BottomAppBarComponent() {

            override val id: String = UUID.randomUUID().toString()
        }

        data class Visible(
            override val id: String,
            @MenuRes val menu: Int,
            @DrawableRes val navigationIcon: Int? = null,
            val data: Any? = null,
        ) : BottomAppBarComponent()
    }

    sealed class FabComponent {

        abstract val id: String

        object Gone : FabComponent() {

            override val id: String = UUID.randomUUID().toString()
        }

        data class Visible(
            override val id: String,
            @DrawableRes val icon: Int,
            val data: Any? = null,
        ) : FabComponent()
    }
}
