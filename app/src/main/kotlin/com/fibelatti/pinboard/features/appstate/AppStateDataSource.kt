package com.fibelatti.pinboard.features.appstate

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.core.functional.SingleRunner
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStateDataSource @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider,
    private val navigationActionHandler: NavigationActionHandler,
    private val postActionHandler: PostActionHandler,
    private val searchActionHandler: SearchActionHandler,
    private val tagActionHandler: TagActionHandler,
    private val noteActionHandler: NoteActionHandler,
    private val popularActionHandler: PopularActionHandler,
    private val singleRunner: SingleRunner,
    private val connectivityInfoProvider: ConnectivityInfoProvider
) : AppStateRepository {

    private val currentContent = MutableLiveData<Content>()

    init {
        reset()
    }

    override fun getContent(): LiveData<Content> = currentContent

    override fun reset() {
        updateContent(getInitialContent())
    }

    override suspend fun runAction(action: Action) {
        singleRunner.afterPrevious {
            val content = currentContent.value ?: getInitialContent()

            val newContent = when (action) {
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
    fun getInitialContent(): Content {
        return PostListContent(
            category = All,
            title = resourceProvider.getString(R.string.posts_title_all),
            posts = null,
            showDescription = userRepository.getShowDescriptionInLists(),
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage,
            isConnected = connectivityInfoProvider.isConnected()
        )
    }

    @VisibleForTesting
    fun updateContent(content: Content) {
        currentContent.postValue(content)
    }
}
