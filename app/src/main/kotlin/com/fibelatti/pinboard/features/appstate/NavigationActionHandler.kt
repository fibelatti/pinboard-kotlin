package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import javax.inject.Inject

class NavigationActionHandler @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val connectivityInfoProvider: ConnectivityInfoProvider
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
        }
    }

    private fun navigateBack(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType(currentContent, ContentWithHistory::previousContent)
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
            sortType = NewestFirst,
            searchParameters = SearchParameters(),
            shouldLoad = if (connectivityInfoProvider.isConnected()) ShouldLoadFirstPage else Loaded,
            isConnected = connectivityInfoProvider.isConnected()
        )
    }

    private fun viewPost(action: ViewPost, currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            PostDetailContent(action.post, previousContent = it)
        }
    }

    private fun viewSearch(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            SearchContent(it.searchParameters, shouldLoadTags = true, previousContent = it)
        }
    }

    private fun viewAddPost(currentContent: Content): Content {
        return runOnlyForCurrentContentOfType<PostListContent>(currentContent) {
            AddPostContent(previousContent = it)
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
                note = Either.left(connectivityInfoProvider.isConnected()),
                isConnected = connectivityInfoProvider.isConnected(),
                previousContent = it
            )
        }
    }
}
