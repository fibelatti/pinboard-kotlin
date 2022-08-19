package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    private val userRepository: UserRepository,
    private val actionHandlers: Map<Class<out Action>, @JvmSuppressWildcards ActionHandler<*>>,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
) : AppStateRepository {

    private val userActions: MutableSharedFlow<suspend (Content) -> Content> = MutableSharedFlow()

    override val content: Flow<Content> = userActions
        .scan(getInitialContent()) { content, actions -> actions(content) }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = getInitialContent(),
        )

    override suspend fun reset() {
        userActions.emit { getInitialContent() }
    }

    override suspend fun runAction(action: Action) {
        userActions.emit { content ->
            return@emit if (action is AuthAction) {
                when (action) {
                    is UserLoggedIn -> getInitialPostListContent()
                    is UserLoggedOut, is UserUnauthorized -> {
                        userRepository.clearAuthToken()
                        LoginContent(isUnauthorized = action is UserUnauthorized)
                    }
                }
            } else {
                @Suppress("UNCHECKED_CAST")
                val handler = actionHandlers[action.getActionType()] as? ActionHandler<Action>
                handler?.runAction(action, content) ?: content
            }
        }
    }

    private fun Action.getActionType(): Class<out Action> {
        val thisType = this::class.java
        if (thisType == Action::class.java) return thisType

        var supertype = thisType.superclass
        while (supertype.superclass != Action::class.java) {
            supertype = supertype.superclass
        }

        @Suppress("UNCHECKED_CAST")
        return supertype as Class<Action>
    }

    private fun getInitialContent(): Content = if (userRepository.hasAuthToken()) {
        getInitialPostListContent()
    } else {
        LoginContent()
    }

    private fun getInitialPostListContent(): Content = PostListContent(
        category = All,
        posts = null,
        showDescription = userRepository.showDescriptionInLists,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = connectivityInfoProvider.isConnected(),
    )
}
