package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject

class PostActionHandler @Inject constructor(
    private val userRepository: UserRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<PostAction>() {

    override suspend fun runAction(action: PostAction, currentContent: Content): Content = when (action) {
        is Refresh -> refresh(currentContent, force = action.force)
        is SetPosts -> setPosts(action, currentContent)
        is GetNextPostPage -> getNextPostPage(currentContent)
        is SetNextPostPage -> setNextPostPage(action, currentContent)
        is ToggleSorting -> toggleSorting(currentContent)
        is EditPost -> editPost(action, currentContent)
        is EditPostFromShare -> editPostFromShare(action)
        is PostSaved -> postSaved(action, currentContent)
        is PostDeleted -> postDeleted(currentContent)
    }

    private fun refresh(currentContent: Content, force: Boolean): Content {
        return if (currentContent is PostListContent) {
            currentContent.copy(
                shouldLoad = when {
                    !connectivityInfoProvider.isConnected() -> Loaded
                    force -> ShouldForceLoad
                    else -> ShouldLoadFirstPage
                },
                isConnected = connectivityInfoProvider.isConnected(),
                canForceSync = !force,
            )
        } else {
            currentContent
        }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) { currentPostList ->
            val posts = action.postListResult.posts.takeIf { it.isNotEmpty() }?.let { newList ->
                PostList(
                    list = newList,
                    totalCount = action.postListResult.totalCount,
                    canPaginate = action.postListResult.canPaginate,
                )
            }
            currentPostList.copy(
                posts = posts,
                shouldLoad = if (action.postListResult.upToDate) Loaded else Syncing,
            )
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
            if (currentPostList.posts != null) {
                val currentList = currentPostList.currentList
                val updatedList = currentList.union(action.postListResult.posts).toList()
                val posts = PostList(
                    list = updatedList,
                    totalCount = action.postListResult.totalCount,
                    canPaginate = action.postListResult.canPaginate,
                )

                currentPostList.copy(posts = posts, shouldLoad = Loaded)
            } else {
                currentContent
            }
        }
    }

    private fun toggleSorting(currentContent: Content): Content {
        return if (currentContent is PostListContent) {
            if (connectivityInfoProvider.isConnected()) {
                currentContent.copy(
                    sortType = when (currentContent.sortType) {
                        is NewestFirst -> OldestFirst
                        is OldestFirst -> NewestFirst
                    },
                    shouldLoad = ShouldLoadFirstPage,
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
                previousContent = currentContent,
            )
        } else {
            currentContent
        }
    }

    private fun editPostFromShare(action: EditPostFromShare): Content {
        return EditPostContent(
            post = action.post,
            previousContent = ExternalContent,
        )
    }

    private fun postSaved(action: PostSaved, currentContent: Content): Content {
        return when (currentContent) {
            is PostListContent -> currentContent.copy(shouldLoad = ShouldLoadFirstPage)
            is AddPostContent -> currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
            is PostDetailContent -> {
                currentContent.copy(
                    post = action.post,
                    previousContent = currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
                )
            }

            is EditPostContent -> {
                when (currentContent.previousContent) {
                    is PostDetailContent -> {
                        val postDetail = currentContent.previousContent

                        postDetail.copy(
                            post = action.post,
                            previousContent = postDetail.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
                        )
                    }

                    is PostListContent -> {
                        currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                    }

                    else -> currentContent.previousContent
                }
            }

            is PopularPostDetailContent -> {
                val updatedCurrentContent = currentContent.copy(
                    previousContent = currentContent.previousContent.copy(
                        previousContent = currentContent.previousContent.previousContent.copy(
                            shouldLoad = ShouldLoadFirstPage,
                        ),
                    ),
                )

                if (userRepository.editAfterSharing is EditAfterSharing.AfterSaving) {
                    EditPostContent(post = action.post, previousContent = updatedCurrentContent)
                } else {
                    updatedCurrentContent
                }
            }

            is PopularPostsContent -> {
                val updatedCurrentContent = currentContent.copy(
                    previousContent = currentContent.previousContent.copy(
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                )

                if (userRepository.editAfterSharing !is EditAfterSharing.SkipEdit) {
                    EditPostContent(post = action.post, previousContent = updatedCurrentContent)
                } else {
                    updatedCurrentContent
                }
            }

            else -> currentContent
        }
    }

    private fun postDeleted(currentContent: Content): Content {
        return if (currentContent is ContentWithHistory) {
            val previousContent = currentContent.previousContent
            when {
                currentContent is PostListContent -> {
                    currentContent.copy(shouldLoad = ShouldLoadFirstPage)
                }

                previousContent is PostListContent -> {
                    previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                }

                previousContent is PostDetailContent -> {
                    previousContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                }

                else -> previousContent
            }
        } else {
            currentContent
        }
    }
}
