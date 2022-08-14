package com.fibelatti.pinboard.features.appstate

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    private val userRepository: UserRepository,
    private val actionHandlers: Map<Class<out Action>, @JvmSuppressWildcards ActionHandler<*>>,
    private val singleRunner: SingleRunner,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : AppStateRepository {

    private val currentContent: MutableStateFlow<Content> = MutableStateFlow(getInitialContent())

    override fun getContent(): Flow<Content> = currentContent.asStateFlow()

    override fun reset() {
        updateContent(getInitialContent())
    }

    override suspend fun runAction(action: Action) {
        singleRunner.afterPrevious {
            val content = currentContent.value

            val newContent = if (action is AuthAction) {
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

            if (newContent != content) {
                updateContent(newContent)
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

    @VisibleForTesting
    fun getInitialContent(): Content = if (userRepository.hasAuthToken()) {
        getInitialPostListContent()
    } else {
        LoginContent()
    }

    private fun getInitialPostListContent() = PostListContent(
        category = All,
        posts = null,
        showDescription = userRepository.showDescriptionInLists,
        sortType = NewestFirst,
        searchParameters = SearchParameters(),
        shouldLoad = ShouldLoadFirstPage,
        isConnected = connectivityInfoProvider.isConnected(),
    )

    @VisibleForTesting
    fun updateContent(content: Content) {
        currentContent.value = content
    }
}
