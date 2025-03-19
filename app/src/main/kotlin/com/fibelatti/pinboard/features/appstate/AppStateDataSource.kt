package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.core.network.UnauthorizedPluginProvider
import com.fibelatti.pinboard.features.user.domain.GetPreferredSortType
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class AppStateDataSource @Inject constructor(
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
    @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted,
    private val actionHandlers: Map<Class<out Action>, @JvmSuppressWildcards ActionHandler<*>>,
    private val userRepository: UserRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val appModeProvider: AppModeProvider,
    private val unauthorizedPluginProvider: UnauthorizedPluginProvider,
    private val getPreferredSortType: GetPreferredSortType,
) : AppStateRepository {

    private val reducer: MutableSharedFlow<suspend (AppState) -> AppState> = MutableSharedFlow()

    override val appState: StateFlow<AppState> = reducer
        .scan(getInitialAppState()) { appState, reducer -> reducer(appState) }
        .combine(appModeProvider.appMode) { appState, appMode -> appState.copy(appMode = appMode) }
        .flowOn(dispatcher)
        .stateIn(scope = scope, started = sharingStarted, initialValue = getInitialAppState())

    init {
        unauthorizedPluginProvider.unauthorized
            .onEach { appMode -> runAction(UserUnauthorized(appMode = appMode)) }
            .launchIn(scope)
    }

    override suspend fun runAction(action: Action) {
        withContext(NonCancellable) {
            reduce(action)
        }
    }

    private suspend fun reduce(action: Action) {
        reducer.emit { appState: AppState ->
            Timber.d("Reducing (action=${action.prettyPrint()}, appState=${appState.prettyPrint()})")

            val newContent: Content = when (action) {
                is AppAction -> {
                    when (action) {
                        is MultiPanelAvailabilityChanged -> appState.content
                        is Reset -> getInitialContent()
                    }
                }

                is AuthAction -> {
                    when (action) {
                        is UserLoggedIn -> {
                            unauthorizedPluginProvider.enable(appMode = action.appMode)
                            getInitialPostListContent()
                        }

                        is UserLoginFailed, is UserLoggedOut, is UserUnauthorized -> {
                            appModeProvider.setSelection(appMode = null)
                            unauthorizedPluginProvider.disable(appMode = action.appMode)
                            userRepository.clearAuthToken(appMode = action.appMode)

                            when {
                                action is UserLoginFailed -> appState.content
                                userRepository.userCredentials.first().hasAuthToken() -> getInitialPostListContent()
                                else -> LoginContent()
                            }
                        }
                    }
                }

                else -> {
                    @Suppress("UNCHECKED_CAST")
                    val handler = actionHandlers[action.getActionType()] as? ActionHandler<Action>
                    handler?.runAction(action, appState.content) ?: appState.content
                }
            }

            appState.copy(
                content = newContent,
                multiPanelAvailable = if (action is MultiPanelAvailabilityChanged) {
                    action.available
                } else {
                    appState.multiPanelAvailable
                },
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Action.getActionType(): Class<out Action> {
        val thisType: Class<out Action> = this::class.java
        if (thisType == Action::class.java) return thisType

        var supertype: Class<out Action> = thisType.superclass as Class<out Action>
        while (supertype.superclass != Action::class.java) {
            supertype = supertype.superclass as Class<out Action>
        }

        return supertype
    }

    private fun getInitialAppState(): AppState = AppState(
        appMode = appModeProvider.appMode.value,
        content = getInitialContent(),
        multiPanelAvailable = false,
    )

    private fun getInitialContent(): Content {
        return if (userRepository.userCredentials.value.hasAuthToken()) {
            getInitialPostListContent()
        } else {
            LoginContent()
        }
    }

    private fun getInitialPostListContent(): Content = PostListContent(
        category = All,
        posts = null,
        showDescription = userRepository.showDescriptionInLists,
        sortType = getPreferredSortType(),
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = connectivityInfoProvider.isConnected(),
    )
}
