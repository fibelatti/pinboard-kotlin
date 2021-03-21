package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.extension.orZero
import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class Content

data class PostListContent(
    val category: ViewCategory,
    val posts: PostList?,
    val showDescription: Boolean,
    val sortType: SortType,
    val searchParameters: SearchParameters,
    val shouldLoad: ShouldLoad,
    val isConnected: Boolean = true,
    val canForceSync: Boolean = true,
) : ContentWithHistory() {

    override val previousContent: Content = ExternalContent

    val totalCount: Int
        get() = posts?.totalCount.orZero()
    val currentCount: Int
        get() = posts?.list?.size.orZero()
    val currentList: List<Post>
        get() = posts?.list ?: emptyList()
}

private interface ContentHistory {
    val previousContent: Content
}

sealed class ContentWithHistory : Content(), ContentHistory

data class PostDetailContent(
    val post: Post,
    override val previousContent: PostListContent
) : ContentWithHistory()

data class ExternalBrowserContent(
    val post: Post,
    override val previousContent: Content
) : ContentWithHistory()

data class SearchContent(
    val searchParameters: SearchParameters,
    val availableTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val shouldLoadTags: Boolean = true,
    override val previousContent: PostListContent
) : ContentWithHistory()

data class AddPostContent(
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val defaultTags: List<Tag>,
    override val previousContent: PostListContent,
) : ContentWithHistory()

data class EditPostContent(
    val post: Post,
    override val previousContent: Content
) : ContentWithHistory()

data class TagListContent(
    val tags: List<Tag>,
    val shouldLoad: Boolean,
    val isConnected: Boolean = true,
    override val previousContent: PostListContent
) : ContentWithHistory()

data class NoteListContent(
    val notes: List<Note>,
    val shouldLoad: Boolean,
    val isConnected: Boolean = true,
    override val previousContent: PostListContent
) : ContentWithHistory()

data class NoteDetailContent(
    val id: String,
    val note: Either<Boolean, Note>,
    val isConnected: Boolean = true,
    override val previousContent: NoteListContent
) : ContentWithHistory()

data class PopularPostsContent(
    val posts: List<Post>,
    val shouldLoad: Boolean,
    val isConnected: Boolean = true,
    override val previousContent: PostListContent
) : ContentWithHistory()

data class PopularPostDetailContent(
    val post: Post,
    override val previousContent: PopularPostsContent
) : ContentWithHistory()

data class UserPreferencesContent(
    val periodicSync: PeriodicSync,
    val appearance: Appearance,
    val preferredDateFormat: PreferredDateFormat,
    val preferredDetailsView: PreferredDetailsView,
    val autoFillDescription: Boolean,
    val showDescriptionInLists: Boolean,
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val editAfterSharing: EditAfterSharing,
    val defaultTags: List<Tag>,
    override val previousContent: PostListContent,
) : ContentWithHistory()

/**
 * Used when sharing URLs to the app. It usually indicates that the app should be finished so that
 * the user can return to the origin of the deeplink.
 */
object ExternalContent : Content()
