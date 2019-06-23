package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.pinboard.core.extension.isConnected
import javax.inject.Inject

class PostActionHandler @Inject constructor(
    private val connectivityManager: ConnectivityManager?
) {

    fun runAction(action: PostAction, currentContent: Content): Content {
        return when (action) {
            is Refresh -> refresh(currentContent)
            is SetPosts -> setPosts(action, currentContent)
            is ToggleSorting -> toggleSorting(currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return if (currentContent is PostList && !currentContent.shouldLoad) {
            currentContent.copy(
                shouldLoad = connectivityManager.isConnected(),
                isConnected = connectivityManager.isConnected()
            )
        } else {
            currentContent
        }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        return if (currentContent is PostList) {
            currentContent.copy(
                posts = action.posts,
                shouldLoad = false
            )
        } else {
            currentContent
        }
    }

    private fun toggleSorting(currentContent: Content): Content {
        return if (currentContent is PostList && !currentContent.shouldLoad) {
            if (connectivityManager.isConnected()) {
                currentContent.copy(
                    sortType = when (currentContent.sortType) {
                        is NewestFirst -> OldestFirst
                        is OldestFirst -> NewestFirst
                    },
                    shouldLoad = true
                )
            } else {
                currentContent.copy(isConnected = false)
            }
        } else {
            currentContent
        }
    }
}
