package com.fibelatti.pinboard.features.appstate

import android.net.ConnectivityManager
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtilFactory
import javax.inject.Inject

class PostActionHandler @Inject constructor(
    private val connectivityManager: ConnectivityManager?,
    private val postListDiffUtilFactory: PostListDiffUtilFactory
) : ActionHandler<PostAction>() {

    override fun runAction(action: PostAction, currentContent: Content): Content {
        return when (action) {
            is Refresh -> refresh(currentContent)
            is SetPosts -> setPosts(action, currentContent)
            is GetNextPostPage -> getNextPostPage(currentContent)
            is SetNextPostPage -> setNextPostPage(action, currentContent)
            is ToggleSorting -> toggleSorting(currentContent)
            is EditPost -> editPost(action, currentContent)
            is PostSaved -> postSaved(action, currentContent)
            is PostDeleted -> postDeleted(currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return if (currentContent is PostList && currentContent.shouldLoad is Loaded) {
            currentContent.copy(
                shouldLoad = if (connectivityManager.isConnected()) ShouldLoadFirstPage else Loaded,
                isConnected = connectivityManager.isConnected()
            )
        } else {
            currentContent
        }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostList>(currentContent) { currentPostList ->
            val posts = action.posts?.let { (count, list) ->
                Triple(count, list, postListDiffUtilFactory.create(currentPostList.currentList, list))
            }

            currentPostList.copy(posts = posts, shouldLoad = Loaded)
        }
    }

    private fun getNextPostPage(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostList>(currentContent) {
            if (it.posts != null) {
                it.copy(shouldLoad = ShouldLoadNextPage(offset = it.currentCount))
            } else {
                currentContent
            }
        }
    }

    private fun setNextPostPage(action: SetNextPostPage, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostList>(currentContent) { currentPostList ->
            if (currentPostList.posts != null && action.posts != null) {
                val (_, currentList) = currentPostList.posts
                val (updatedCount, newList) = action.posts
                val updatedList = currentList.plus(newList)
                val posts = Triple(updatedCount, updatedList, postListDiffUtilFactory.create(currentList, updatedList))

                currentPostList.copy(posts = posts, shouldLoad = Loaded)
            } else {
                currentContent
            }
        }
    }

    private fun toggleSorting(currentContent: Content): Content {
        return if (currentContent is PostList && currentContent.shouldLoad is Loaded) {
            if (connectivityManager.isConnected()) {
                currentContent.copy(
                    sortType = when (currentContent.sortType) {
                        is NewestFirst -> OldestFirst
                        is OldestFirst -> NewestFirst
                    },
                    shouldLoad = ShouldLoadFirstPage
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
                currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
            }
            is EditPostView -> {
                val postDetail = currentContent.previousContent

                postDetail.copy(
                    post = action.post,
                    previousContent = postDetail.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                )
            }
            else -> currentContent
        }
    }

    private fun postDeleted(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostDetail>(currentContent) {
            it.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
        }
    }
}
