package com.fibelatti.pinboard.features.main

import androidx.lifecycle.SavedStateHandle
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.LocalNetworkAccessProvider
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.ScrollDirection
import com.fibelatti.pinboard.features.appstate.All
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.Content
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.MultiPanelAvailabilityChanged
import com.fibelatti.pinboard.features.appstate.NavigateBack
import com.fibelatti.pinboard.features.appstate.PostListContent
import com.fibelatti.pinboard.features.appstate.Reset
import com.fibelatti.pinboard.features.appstate.ViewPost
import com.fibelatti.pinboard.features.main.reducer.MainStateReducer
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
    appStateRepository: AppStateRepository,
    mainStateReducers: Map<Class<out Content>, @JvmSuppressWildcards MainStateReducer>,
    private val userRepository: UserRepository,
    private val localNetworkAccessProvider: LocalNetworkAccessProvider,
) : BaseViewModel(scope, appStateRepository) {

    private val reducer: MutableSharedFlow<suspend (MainState) -> MainState> = MutableSharedFlow()

    val state: StateFlow<MainState> = reducer
        .scan(initial = MainState()) { state, reducer -> reducer(state) }
        .stateIn(scope = scope, started = sharingStarted, initialValue = MainState())

    private val actionButtonClicks = MutableSharedFlow<Pair<ContentType, Any?>>()
    private val menuItemClicks = MutableSharedFlow<Triple<ContentType, MainState.MenuItemComponent, Any?>>()
    private val fabClicks = MutableSharedFlow<Pair<ContentType, Any?>>()

    private val _localNetworkPermissionRequired: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * True when the Linkding instance the user is already logged into sits in the local network
     * and cannot be reached until [LocalNetworkAccessProvider.PERMISSION] is granted. Users who
     * logged in before the permission existed may not go through the login screen again, so without
     * this their bookmarks would silently stop syncing.
     *
     * This is state rather than an event because it is set while the app is starting up, before the
     * screen observing it is ready. It must be cleared with [localNetworkPermissionHandled] once
     * requested.
     */
    val localNetworkPermissionRequired: StateFlow<Boolean> = _localNetworkPermissionRequired.asStateFlow()

    init {
        appStateRepository.appState
            .onEach { appState ->
                mainStateReducers[appState.content::class.java]?.let { mainStateReducer: MainStateReducer ->
                    reducer.emit { current: MainState -> mainStateReducer(current, appState) }
                }

                if (appState.content is PostListContent) {
                    consumeDeepLink(appState.content)
                }
            }
            .launchIn(scope)

        appStateRepository.appState
            // The login screen requests the permission itself, using the instance being logged into.
            // Distinct so the check runs once on entering the state rather than on every content update,
            // which would otherwise re-raise the flag and show the prompt again.
            .map { appState -> appState.appMode to (appState.content is LoginContent) }
            .distinctUntilChanged()
            .filter { (appMode, isLoggingIn) -> appMode == AppMode.LINKDING && !isLoggingIn }
            .onEach {
                _localNetworkPermissionRequired.update {
                    localNetworkAccessProvider.isPermissionRequired(userRepository.linkdingInstanceUrl)
                }
            }
            .launchIn(scope)
    }

    fun localNetworkPermissionHandled() {
        _localNetworkPermissionRequired.value = false
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

    fun handleDeeplink(postId: String, openEditor: Boolean) {
        savedStateHandle[KEY_DEEP_LINK_POST_ID] = postId
        savedStateHandle[KEY_DEEP_LINK_OPEN_EDITOR] = openEditor

        runAction(All)
    }

    private fun consumeDeepLink(content: PostListContent) {
        val pendingDeeplinkPostId: String = savedStateHandle[KEY_DEEP_LINK_POST_ID] ?: return
        val posts: List<Post> = content.posts?.list ?: return
        val post: Post = posts.find { it.id == pendingDeeplinkPostId } ?: return
        val openEditor: Boolean? = savedStateHandle[KEY_DEEP_LINK_OPEN_EDITOR]

        runAction(if (openEditor == true) EditPost(post = post) else ViewPost(post))

        savedStateHandle.remove<String?>(KEY_DEEP_LINK_POST_ID)
        savedStateHandle.remove<Boolean?>(KEY_DEEP_LINK_OPEN_EDITOR)
    }

    private companion object {

        private const val KEY_DEEP_LINK_POST_ID: String = "deeplinkPostId"
        private const val KEY_DEEP_LINK_OPEN_EDITOR: String = "deeplinkOpenEditor"
    }
}
