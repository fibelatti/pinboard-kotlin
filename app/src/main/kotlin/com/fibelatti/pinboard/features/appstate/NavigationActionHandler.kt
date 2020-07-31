package com.fibelatti.pinboard.features.appstate

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.orFalse
import com.fibelatti.core.functional.Either
import com.fibelatti.core.functional.catching
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.IoScope
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NavigationActionHandler @Inject constructor(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    private val resourceProvider: ResourceProvider,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    @IoScope private val markAsReadRequestScope: CoroutineScope
) : ActionHandler<NavigationAction>() {

    override suspend fun runAction(action: NavigationAction, currentContent: Content): Content {
        return when (action) {
            is NavigateBack -> navigateBack(currentContent)
            is ViewCategory -> viewCategory(action)
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
        return runOnlyForCurrentContentOfType<ContentWithHistory>(currentContent) {
            if (currentContent is UserPreferencesContent) {
                currentContent.previousContent.copy(
                    showDescription = userRepository.getShowDescriptionInLists()
                )
            } else {
                it.previousContent
            }
        }
    }

    private fun viewCategory(action: ViewCategory): Content {
        return PostListContent(
            category = action,
            title = when (action) {
                All -> resourceProvider.getString(R.string.posts_title_all)
                Recent -> resourceProvider.getString(R.string.posts_title_recent)
                Public -> resourceProvider.getString(R.string.posts_title_public)
                Private -> resourceProvider.getString(R.string.posts_title_private)
                Unread -> resourceProvider.getString(R.string.posts_title_unread)
                Untagged -> resourceProvider.getString(R.string.posts_title_untagged)
            },
            posts = null,
            showDescription = userRepository.getShowDescriptionInLists(),
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = ShouldLoadFirstPage,
            isConnected = connectivityInfoProvider.isConnected()
        )
    }

    private fun viewPost(action: ViewPost, currentContent: Content): Content {
        val preferredDetailsView = userRepository.getPreferredDetailsView()

        return when (currentContent) {
            is PostListContent -> {
                when (preferredDetailsView) {
                    is PreferredDetailsView.InAppBrowser -> {
                        val shouldLoad: ShouldLoad = markAsRead(action.post)
                        PostDetailContent(
                            action.post,
                            previousContent = currentContent.copy(shouldLoad = shouldLoad)
                        )
                    }
                    is PreferredDetailsView.ExternalBrowser -> {
                        val shouldLoad: ShouldLoad = markAsRead(action.post)
                        ExternalBrowserContent(
                            action.post,
                            previousContent = currentContent.copy(shouldLoad = shouldLoad)
                        )
                    }
                    PreferredDetailsView.Edit -> {
                        EditPostContent(
                            post = action.post,
                            previousContent = currentContent
                        )
                    }
                }
            }
            is PopularPostsContent -> {
                if (preferredDetailsView is PreferredDetailsView.ExternalBrowser) {
                    ExternalBrowserContent(action.post, previousContent = currentContent)
                } else {
                    PopularPostDetailContent(action.post, previousContent = currentContent)
                }
            }
            else -> currentContent
        }
    }

    @VisibleForTesting
    fun markAsRead(post: Post): ShouldLoad {
        return if (post.readLater && userRepository.getMarkAsReadOnOpen()) {
            markAsReadRequestScope.launch {
                catching {
                    postsRepository.add(
                        url = post.url,
                        title = post.title,
                        description = post.description,
                        private = post.private,
                        readLater = false,
                        tags = post.tags,
                        replace = true
                    )
                }
            }
            ShouldLoadFirstPage
        } else {
            Loaded
        }
    }

    private fun viewSearch(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            SearchContent(it.searchParameters, shouldLoadTags = true, previousContent = it)
        }
    }

    private fun viewAddPost(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            AddPostContent(
                defaultPrivate = userRepository.getDefaultPrivate().orFalse(),
                defaultReadLater = userRepository.getDefaultReadLater().orFalse(),
                previousContent = it
            )
        }
    }

    private fun viewTags(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            TagListContent(
                tags = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
                previousContent = it
            )
        }
    }

    private fun viewNotes(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            NoteListContent(
                notes = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
                previousContent = it
            )
        }
    }

    private fun viewNote(action: ViewNote, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<NoteListContent>(currentContent) {
            NoteDetailContent(
                id = action.id,
                note = Either.Left(connectivityInfoProvider.isConnected()),
                isConnected = connectivityInfoProvider.isConnected(),
                previousContent = it
            )
        }
    }

    private fun viewPopular(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            PopularPostsContent(
                posts = emptyList(),
                shouldLoad = connectivityInfoProvider.isConnected(),
                isConnected = connectivityInfoProvider.isConnected(),
                previousContent = it
            )
        }
    }

    private fun viewPreferences(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            UserPreferencesContent(
                appearance = userRepository.getAppearance(),
                preferredDetailsView = userRepository.getPreferredDetailsView(),
                autoFillDescription = userRepository.getAutoFillDescription(),
                showDescriptionInLists = userRepository.getShowDescriptionInLists(),
                defaultPrivate = userRepository.getDefaultPrivate().orFalse(),
                defaultReadLater = userRepository.getDefaultReadLater().orFalse(),
                editAfterSharing = userRepository.getEditAfterSharing(),
                previousContent = it
            )
        }
    }
}
