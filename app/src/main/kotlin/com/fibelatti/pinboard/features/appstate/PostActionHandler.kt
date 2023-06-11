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
        return currentContent.reduce<PostListContent> { postListContent ->
            postListContent.copy(
                shouldLoad = when {
                    !connectivityInfoProvider.isConnected() -> Loaded
                    force -> ShouldForceLoad
                    else -> ShouldLoadFirstPage
                },
                isConnected = connectivityInfoProvider.isConnected(),
                canForceSync = !force,
            )
        }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            val posts = action.postListResult.posts.takeIf { it.isNotEmpty() }?.let { newList ->
                PostList(
                    list = newList,
                    totalCount = action.postListResult.totalCount,
                    canPaginate = action.postListResult.canPaginate,
                    shouldScrollToTop = true,
                )
            }
            postListContent.copy(
                posts = posts,
                shouldLoad = if (action.postListResult.upToDate) Loaded else Syncing,
            )
        }
    }

    private fun getNextPostPage(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            if (postListContent.posts != null) {
                postListContent.copy(shouldLoad = ShouldLoadNextPage(offset = postListContent.currentCount))
            } else {
                currentContent
            }
        }
    }

    private fun setNextPostPage(action: SetNextPostPage, currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            if (postListContent.posts != null) {
                val updatedList = postListContent.currentList.union(action.postListResult.posts).toList()
                val posts = PostList(
                    list = updatedList,
                    totalCount = action.postListResult.totalCount,
                    canPaginate = action.postListResult.canPaginate,
                    shouldScrollToTop = false,
                )

                postListContent.copy(posts = posts, shouldLoad = Loaded)
            } else {
                currentContent
            }
        }
    }

    private fun toggleSorting(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            if (connectivityInfoProvider.isConnected()) {
                postListContent.copy(
                    sortType = when (postListContent.sortType) {
                        is NewestFirst -> OldestFirst
                        is OldestFirst -> NewestFirst
                    },
                    shouldLoad = ShouldLoadFirstPage,
                )
            } else {
                postListContent.copy(isConnected = false)
            }
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
        return currentContent.reduce<PostListContent> { postListContent ->
            postListContent.copy(shouldLoad = ShouldLoadFirstPage)
        }.reduce<AddPostContent> { addPostContent ->
            addPostContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
        }.reduce<PostDetailContent> { postDetailContent ->
            postDetailContent.copy(
                post = action.post,
                previousContent = postDetailContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
            )
        }.reduce<EditPostContent> { editPostContent ->
            when (editPostContent.previousContent) {
                is PostDetailContent -> {
                    val postDetail = editPostContent.previousContent

                    postDetail.copy(
                        post = action.post,
                        previousContent = postDetail.previousContent.copy(shouldLoad = ShouldLoadFirstPage),
                    )
                }

                is PostListContent -> {
                    editPostContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
                }

                else -> editPostContent.previousContent
            }
        }.reduce<PopularPostDetailContent> { popularPostDetailContent ->
            val updatedCurrentContent = popularPostDetailContent.copy(
                previousContent = popularPostDetailContent.previousContent.copy(
                    previousContent = popularPostDetailContent.previousContent.previousContent.copy(
                        shouldLoad = ShouldLoadFirstPage,
                    ),
                ),
            )

            if (userRepository.editAfterSharing is EditAfterSharing.AfterSaving) {
                EditPostContent(post = action.post, previousContent = updatedCurrentContent)
            } else {
                updatedCurrentContent
            }
        }.reduce<PopularPostsContent> { popularPostsContent ->
            val updatedCurrentContent = popularPostsContent.copy(
                previousContent = popularPostsContent.previousContent.copy(
                    shouldLoad = ShouldLoadFirstPage,
                ),
            )

            if (userRepository.editAfterSharing !is EditAfterSharing.SkipEdit) {
                EditPostContent(post = action.post, previousContent = updatedCurrentContent)
            } else {
                updatedCurrentContent
            }
        }
    }

    private fun postDeleted(currentContent: Content): Content {
        return currentContent.reduce<ContentWithHistory> { contentWithHistory ->
            val previousContent = contentWithHistory.previousContent
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
        }
    }
}
