package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.extension.isConnected
import javax.inject.Inject

class TagActionHandler @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager?
) : ActionHandler<TagAction>() {

    override fun runAction(action: TagAction, currentContent: Content): Content {
        return when (action) {
            is RefreshTags -> refresh(currentContent)
            is SetTags -> setTags(action, currentContent)
            is PostsForTag -> postsForTag(action)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<TagList>(currentContent) {
            it.copy(
                shouldLoad = connectivityManager.isConnected(),
                isConnected = connectivityManager.isConnected()
            )
        }
    }

    private fun setTags(action: SetTags, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<TagList>(currentContent) {
            it.copy(
                tags = action.tags,
                shouldLoad = false
            )
        }
    }

    private fun postsForTag(action: PostsForTag): Content {
        return PostList(
            category = All,
            title = resourceProvider.getString(R.string.posts_title_all),
            posts = emptyList(),
            sortType = NewestFirst,
            searchParameters = SearchParameters(tags = listOf(action.tag)),
            shouldLoad = true,
            isConnected = connectivityManager.isConnected()
        )
    }
}
