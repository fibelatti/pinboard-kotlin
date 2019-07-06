package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.pinboard.core.extension.isConnected
import javax.inject.Inject

class PostActionHandler @Inject constructor(
    private val connectivityManager: ConnectivityManager?
) : ActionHandler<PostAction>() {

    override fun runAction(action: PostAction, currentContent: Content): Content {
        return when (action) {
            is Refresh -> refresh(currentContent)
            is SetPosts -> setPosts(action, currentContent)
            is ToggleSorting -> toggleSorting(currentContent)
            is EditPost -> editPost(action, currentContent)
            is PostSaved -> postSaved(action, currentContent)
            is PostDeleted -> postDeleted(currentContent)
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
        return runOnlyForCurrentContentOfType<PostList>(currentContent) {
            it.copy(
                posts = action.posts,
                shouldLoad = false
            )
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

    private fun editPost(action: EditPost, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostDetail>(currentContent) {
            EditPostView(
                post = action.post,
                previousContent = it
            )
        }
    }

    private fun postSaved(action: PostSaved, currentContent: Content): Content {
        return when (currentContent) {
            is AddPostView -> {
                currentContent.previousContent.copy(shouldLoad = true)
            }
            is EditPostView -> {
                val postDetail = currentContent.previousContent

                postDetail.copy(
                    post = action.post,
                    previousContent = postDetail.previousContent.copy(shouldLoad = true)
                )
            }
            else -> currentContent
        }
    }

    private fun postDeleted(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostDetail>(currentContent) {
            it.previousContent.copy(shouldLoad = true)
        }
    }
}
