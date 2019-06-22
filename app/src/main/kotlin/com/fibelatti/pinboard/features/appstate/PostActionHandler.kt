package com.fibelatti.pinboard.features.appstate

import javax.inject.Inject

class PostActionHandler @Inject constructor() {

    fun runAction(action: PostAction, currentContent: Content): Content {
        return when (action) {
            is Refresh -> refresh(currentContent)
            is SetPosts -> setPosts(action, currentContent)
            is ToggleSorting -> toggleSorting(currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return if (currentContent is PostList) {
            currentContent.copy(shouldLoad = true)
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
        return if (currentContent is PostList) {
            currentContent.copy(
                sortType = when (currentContent.sortType) {
                    is NewestFirst -> OldestFirst
                    is OldestFirst -> NewestFirst
                },
                shouldLoad = true
            )
        } else {
            currentContent
        }
    }
}
