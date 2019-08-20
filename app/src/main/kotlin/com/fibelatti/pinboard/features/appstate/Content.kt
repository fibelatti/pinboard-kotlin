package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.extension.orZero
import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class Content

data class PostListContent(
    val category: ViewCategory,
    val title: String,
    val posts: PostList?,
    val sortType: SortType,
    val searchParameters: SearchParameters,
    val shouldLoad: ShouldLoad,
    val isConnected: Boolean = true
) : Content() {

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
    override val previousContent: PostListContent
) : ContentWithHistory()

data class EditPostContent(
    val post: Post,
    override val previousContent: PostDetailContent
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

data class UserPreferencesContent(
    val appearance: Appearance,
    val autoFillDescription: Boolean,
    val showDescriptionInLists: Boolean,
    val showDescriptionInDetails: Boolean,
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val editAfterSharing: Boolean,
    override val previousContent: PostListContent
) : ContentWithHistory()
