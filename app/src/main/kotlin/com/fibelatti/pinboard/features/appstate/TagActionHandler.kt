package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.pinboard.core.extension.isConnected
import javax.inject.Inject

class TagActionHandler @Inject constructor(
    private val connectivityManager: ConnectivityManager?
) : ActionHandler<TagAction>() {

    override fun runAction(action: TagAction, currentContent: Content): Content {
        return when (action) {
            is RefreshTags -> refresh(currentContent)
            is SetTags -> setTags(action, currentContent)
            is PostsForTag -> postsForTag(action, currentContent)
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

    private fun postsForTag(action: PostsForTag, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<TagList>(currentContent) {
            it.previousContent.copy(
                searchParameters = SearchParameters(tags = listOf(action.tag)),
                shouldLoad = true,
                isConnected = connectivityManager.isConnected()
            )
        }
    }
}
