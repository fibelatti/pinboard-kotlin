package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.pinboard.core.extension.isConnected
import javax.inject.Inject

class TagActionHandler @Inject constructor(
    private val connectivityManager: ConnectivityManager?
) {

    fun runAction(action: TagAction, currentContent: Content): Content {
        return when (action) {
            is RefreshTags -> refresh(currentContent)
            is SetTags -> setTags(action, currentContent)
            is PostsForTag -> postsForTag(action, currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return if (currentContent is TagList) {
            currentContent.copy(
                shouldLoad = connectivityManager.isConnected(),
                isConnected = connectivityManager.isConnected()
            )
        } else {
            currentContent
        }
    }

    private fun setTags(action: SetTags, currentContent: Content): Content {
        return if (currentContent is TagList) {
            currentContent.copy(
                tags = action.tags,
                shouldLoad = false
            )
        } else {
            currentContent
        }
    }

    private fun postsForTag(action: PostsForTag, currentContent: Content): Content {
        return if (currentContent is TagList) {
            return currentContent.previousContent.copy(
                searchParameters = SearchParameters(tags = listOf(action.tag)),
                shouldLoad = true,
                isConnected = connectivityManager.isConnected()
            )
        } else {
            currentContent
        }
    }
}
