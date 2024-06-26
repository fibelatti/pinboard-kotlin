package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.core.network.ConnectivityInfoProvider
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single

@Single
internal class AppStateDataSource(
    private val userRepository: UserRepository,
    private val actionHandlers: Set<ActionHandler<*>>,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val appModeProvider: AppModeProvider,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : AppStateRepository {

    private val userActions: MutableSharedFlow<suspend (Content) -> Content> = MutableSharedFlow()

    override val content: StateFlow<Content> = userActions
        .scan(getInitialContent()) { content, actions -> actions(content) }
        .stateIn(
            scope = scope,
            started = sharingStarted,
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
                val handler: ActionHandler<Action>? = actionHandlers.first {
                    it::class == when (action) {
                        is NavigationAction -> NavigationActionHandler::class
                        is PostAction -> PostActionHandler::class
                        is SearchAction -> SearchActionHandler::class
                        is TagAction -> TagActionHandler::class
                        is NoteAction -> NoteActionHandler::class
                        is PopularAction -> PopularActionHandler::class
                        else -> null
                    }
                } as? ActionHandler<Action>
                handler?.runAction(action, content) ?: content
            }
        }
    }

    private fun getInitialContent(): Content {
        return if (userRepository.hasAuthToken() || AppMode.NO_API == appModeProvider.appMode.value) {
            getInitialPostListContent()
        } else {
            LoginContent()
        }
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
