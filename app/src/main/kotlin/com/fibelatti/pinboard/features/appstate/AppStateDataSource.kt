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
    private val navigationActionHandler: NavigationActionHandler,
    private val postActionHandler: PostActionHandler,
    private val searchActionHandler: SearchActionHandler,
    private val tagActionHandler: TagActionHandler,
    private val noteActionHandler: NoteActionHandler,
    private val popularActionHandler: PopularActionHandler,
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
            val newContent = when (action) {
                is AuthAction -> {
                    when (action) {
                        is UserLoggedIn -> getInitialPostListContent()
                        is UserLoggedOut,
                        is UserUnauthorized -> {
                            userRepository.clearAuthToken()
                            LoginContent(isUnauthorized = action is UserUnauthorized)
                        }
                    }
                }
                is NavigationAction -> navigationActionHandler.runAction(action, content)
                is PostAction -> postActionHandler.runAction(action, content)
                is SearchAction -> searchActionHandler.runAction(action, content)
                is TagAction -> tagActionHandler.runAction(action, content)
                is NoteAction -> noteActionHandler.runAction(action, content)
                is PopularAction -> popularActionHandler.runAction(action, content)
            }

            if (newContent != content) {
                updateContent(newContent)
            }
        }
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
