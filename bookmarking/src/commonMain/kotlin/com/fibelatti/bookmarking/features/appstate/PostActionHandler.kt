package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.core.network.ConnectivityInfoProvider
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import org.koin.core.annotation.Factory

@Factory
internal class PostActionHandler(
    private val userRepository: UserRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<PostAction>() {

    override suspend fun runAction(action: PostAction, currentContent: Content): Content = when (action) {
        is Refresh -> refresh(currentContent, force = action.force)
        is SetPosts -> setPosts(action, currentContent)
        is GetNextPostPage -> getNextPostPage(currentContent)
        is SetNextPostPage -> setNextPostPage(action, currentContent)
        is SetSorting -> setSorting(action, currentContent)
        is EditPost -> editPost(action, currentContent)
        is EditPostFromShare -> editPostFromShare(action)
        is PostSaved -> postSaved(action, currentContent)
        is PostDeleted -> postDeleted(currentContent)
    }

    private fun refresh(currentContent: Content, force: Boolean): Content {
        val body = { postListContent: PostListContent ->
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

        return currentContent
            .reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(previousContent = body(postDetailContent.previousContent))
            }
    }

    private fun setPosts(action: SetPosts, currentContent: Content): Content {
        val body = { postListContent: PostListContent ->
            postListContent.copy(
                posts = action.postListResult.posts.takeIf { it.isNotEmpty() }?.let { newList ->
                    PostList(
                        list = newList,
                        totalCount = action.postListResult.totalCount,
                        canPaginate = action.postListResult.canPaginate,
                    )
                },
                shouldLoad = if (action.postListResult.upToDate) Loaded else Syncing,
            )
        }

        return currentContent
            .reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
            }
    }

    private fun getNextPostPage(currentContent: Content): Content {
        val body = { postListContent: PostListContent ->
            if (postListContent.posts != null) {
                postListContent.copy(shouldLoad = ShouldLoadNextPage(offset = postListContent.currentCount))
            } else {
                postListContent
            }
        }

        return currentContent.reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
            }
    }

    private fun setNextPostPage(action: SetNextPostPage, currentContent: Content): Content {
        val body = { postListContent: PostListContent ->
            if (postListContent.posts != null) {
                val updatedList = postListContent.currentList.union(action.postListResult.posts).toList()
                val posts = PostList(
                    list = updatedList,
                    totalCount = action.postListResult.totalCount,
                    canPaginate = action.postListResult.canPaginate,
                )

                postListContent.copy(posts = posts, shouldLoad = Loaded)
            } else {
                postListContent
            }
        }

        return currentContent
            .reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
            }
    }

    private fun setSorting(action: SetSorting, currentContent: Content): Content {
        val body = { postListContent: PostListContent ->
            if (connectivityInfoProvider.isConnected()) {
                postListContent.copy(
                    sortType = action.sortType,
                    shouldLoad = ShouldLoadFirstPage,
                )
            } else {
                postListContent.copy(isConnected = false)
            }
        }

        return currentContent
            .reduce(body)
            .reduce<PostDetailContent> { postDetailContent ->
                postDetailContent.copy(
                    previousContent = body(postDetailContent.previousContent),
                )
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

    private fun postSaved(action: PostSaved, currentContent: Content): Content = when (currentContent) {
        is PostListContent -> {
            currentContent.copy(shouldLoad = ShouldLoadFirstPage)
        }

        is AddPostContent -> {
            currentContent.previousContent.copy(shouldLoad = ShouldLoadFirstPage)
        }

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

            EditPostContent(post = action.post, previousContent = updatedCurrentContent)
        }

        else -> currentContent
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
