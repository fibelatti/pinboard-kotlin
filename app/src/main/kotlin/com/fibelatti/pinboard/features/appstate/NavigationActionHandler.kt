package com.fibelatti.pinboard.features.appstate

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Either
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NavigationActionHandler @Inject constructor(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : ActionHandler<NavigationAction>() {

    override suspend fun runAction(action: NavigationAction, currentContent: Content): Content {
        return when (action) {
            is NavigateBack -> navigateBack(currentContent)
            is ViewCategory -> viewCategory(action, currentContent)
            is ViewPost -> viewPost(action, currentContent)
            is ViewSearch -> viewSearch(currentContent)
            is AddPost -> viewAddPost(currentContent)
            is ViewTags -> viewTags(currentContent)
            is ViewNotes -> viewNotes(currentContent)
            is ViewNote -> viewNote(action, currentContent)
            is ViewPopular -> viewPopular(currentContent)
            is ViewPreferences -> viewPreferences(currentContent)
        }
    }

    private fun navigateBack(currentContent: Content): Content {
        return currentContent.reduce<ContentWithHistory> { contentWithHistory ->
            if (currentContent is UserPreferencesContent) {
                currentContent.previousContent.copy(
                    showDescription = userRepository.showDescriptionInLists,
                )
            } else {
                contentWithHistory.previousContent
            }
        }
    }

    private fun viewCategory(action: ViewCategory, currentContent: Content): Content {
        return PostListContent(
            category = action,
            // Use the current posts for a smoother transition until the category posts are loaded
            posts = (currentContent as? PostListContent)?.posts,
            showDescription = userRepository.showDescriptionInLists,
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage,
            isConnected = connectivityInfoProvider.isConnected(),
        )
    }

    private suspend fun viewPost(action: ViewPost, currentContent: Content): Content {
        val preferredDetailsView = userRepository.preferredDetailsView

        return currentContent.reduce<PostListContent> { postListContent ->
            when (preferredDetailsView) {
                is PreferredDetailsView.InAppBrowser -> {
                    val shouldLoad: ShouldLoad = markAsRead(action.post)
                    PostDetailContent(
                        post = action.post,
                        previousContent = postListContent.copy(shouldLoad = shouldLoad),
                        isConnected = connectivityInfoProvider.isConnected(),
                    )
                }

                is PreferredDetailsView.ExternalBrowser -> {
                    val shouldLoad: ShouldLoad = markAsRead(action.post)
                    ExternalBrowserContent(
                        action.post,
                        previousContent = postListContent.copy(shouldLoad = shouldLoad),
                    )
                }

                PreferredDetailsView.Edit -> {
                    EditPostContent(
                        post = action.post,
                        previousContent = currentContent,
                    )
                }
            }
        }.reduce<PopularPostsContent> { popularPostsContent ->
            if (preferredDetailsView is PreferredDetailsView.ExternalBrowser) {
                ExternalBrowserContent(action.post, previousContent = currentContent)
            } else {
                PopularPostDetailContent(
                    post = action.post,
                    previousContent = popularPostsContent,
                    isConnected = connectivityInfoProvider.isConnected(),
                )
            }
        }
    }

    @VisibleForTesting
    suspend fun markAsRead(post: Post): ShouldLoad {
        return if (post.readLater && userRepository.markAsReadOnOpen) {
            withContext(NonCancellable) {
                launch {
                    catching {
                        postsRepository.add(
                            url = post.url,
                            title = post.title,
                            description = post.description,
                            private = post.private,
                            readLater = false,
                            tags = post.tags,
                            replace = true,
                            hash = post.hash,
                            time = post.time,
                        )
                    }
                }
            }

            ShouldLoadFirstPage
        } else {
            Loaded
        }
    }

    private fun viewSearch(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            SearchContent(
                searchParameters = postListContent.searchParameters,
                shouldLoadTags = true,
                previousContent = postListContent,
            )
        }
    }

    private fun viewAddPost(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            AddPostContent(
                defaultPrivate = userRepository.defaultPrivate ?: false,
                defaultReadLater = userRepository.defaultReadLater ?: false,
                defaultTags = userRepository.defaultTags,
                previousContent = postListContent,
            )
        }
    }

    private fun viewTags(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            TagListContent(
                tags = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                previousContent = postListContent,
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun viewNotes(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            NoteListContent(
                notes = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                previousContent = postListContent,
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun viewNote(action: ViewNote, currentContent: Content): Content {
        return currentContent.reduce<NoteListContent> { noteListContent ->
            NoteDetailContent(
                id = action.id,
                note = Either.Left(connectivityInfoProvider.isConnected()),
                previousContent = noteListContent,
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun viewPopular(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            PopularPostsContent(
                posts = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                previousContent = postListContent,
                isConnected = connectivityInfoProvider.isConnected(),
            )
        }
    }

    private fun viewPreferences(currentContent: Content): Content {
        return currentContent.reduce<PostListContent> { postListContent ->
            UserPreferencesContent(previousContent = postListContent)
        }
    }
}
