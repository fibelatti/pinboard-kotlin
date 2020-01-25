package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtilFactory
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class PostActionHandler @Inject constructor(
    private val userRepository: UserRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val postListDiffUtilFactory: PostListDiffUtilFactory
) : ActionHandler<PostAction>() {

    override suspend fun runAction(action: PostAction, currentContent: Content): Content {
        return when (action) {
            is Refresh -> refresh(currentContent)
            is SetPosts -> setPosts(action, currentContent)
            is GetNextPostPage -> getNextPostPage(currentContent)
            is SetNextPostPage -> setNextPostPage(action, currentContent)
            is PostsDisplayed -> postsDisplayed(currentContent)
            is ToggleSorting -> toggleSorting(currentContent)
            is EditPost -> editPost(action, currentContent)
            is EditPostFromShare -> editPostFromShare(action)
            is PostSaved -> postSaved(action, currentContent)
            is PostDeleted -> postDeleted(currentContent)
        }
    }

    private fun refresh(currentContent: Content): Content {
        return if (currentContent is PostListContent && currentContent.shouldLoad is Loaded) {
            currentContent.copy(
                shouldLoad = if (connectivityInfoProvider.isConnected()) ShouldLoadFirstPage else Loaded,
                isConnected = connectivityInfoProvider.isConnected()
            )
        } else {
            currentContent
        }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) { currentPostList ->
            val posts = action.posts?.let { (count, list) ->
                PostList(
                    count,
                    list,
                    postListDiffUtilFactory.create(currentPostList.currentList, list)
                )
            }

            currentPostList.copy(posts = posts, shouldLoad = Loaded)
        }
    }

    private fun getNextPostPage(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            if (it.posts != null) {
                it.copy(shouldLoad = ShouldLoadNextPage(offset = it.currentCount))
            } else {
                currentContent
            }
        }
    }

    private fun setNextPostPage(action: SetNextPostPage, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) { currentPostList ->
            if (currentPostList.posts != null && action.posts != null) {
                val currentList = currentPostList.currentList
                val (updatedCount, newList) = action.posts
                val updatedList = currentList.plus(newList)
                val posts = PostList(
                    updatedCount,
                    updatedList,
                    postListDiffUtilFactory.create(currentList, updatedList)
                )

                currentPostList.copy(posts = posts, shouldLoad = Loaded)
            } else {
                currentContent
            }
        }
    }

    private fun postsDisplayed(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            it.copy(posts = it.posts?.copy(alreadyDisplayed = true))
        }
    }

    private fun toggleSorting(currentContent: Content): Content {
        return if (currentContent is PostListContent && currentContent.shouldLoad is Loaded) {
            if (connectivityInfoProvider.isConnected()) {
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
        return if (currentContent is PostListContent || currentContent is PostDetailContent) {
            EditPostContent(
                post = action.post,
                showDescription = userRepository.getShowDescriptionInDetails(),
                previousContent = currentContent
            )
        } else {
            currentContent
        }
    }

    private fun editPostFromShare(action: EditPostFromShare): Content {
        return EditPostContent(
            post = action.post,
            showDescription = userRepository.getShowDescriptionInDetails(),
            previousContent = ExternalContent
        )
    }

    private fun postSaved(action: PostSaved, currentContent: Content): Content {
        return when (currentContent) {
            is AddPostContent -> currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
            is EditPostContent -> {
                when (currentContent.previousContent) {
                    is PostDetailContent -> {
                        val postDetail = currentContent.previousContent

                        postDetail.copy(
                            post = action.post,
                            previousContent = postDetail.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                        )
                    }
                    is PostListContent -> {
                        currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                    }
                    else -> currentContent.previousContent
                }
            }
            is PopularPostDetailContent -> {
                EditPostContent(
                    post = action.post,
                    showDescription = userRepository.getShowDescriptionInDetails(),
                    previousContent = currentContent
                )
            }
            is PopularPostsContent -> {
                EditPostContent(
                    post = action.post,
                    showDescription = userRepository.getShowDescriptionInDetails(),
                    previousContent = currentContent
                )
            }
            else -> currentContent
        }
    }

    private fun postDeleted(currentContent: Content): Content {
        return if (currentContent is ContentWithHistory) {
            when (val previousContent = currentContent.previousContent) {
                is PostListContent -> {
                    previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                }
                is PostDetailContent -> {
                    previousContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                }
                else -> {
                    previousContent
                }
            }
        } else {
            currentContent
        }
    }
}
