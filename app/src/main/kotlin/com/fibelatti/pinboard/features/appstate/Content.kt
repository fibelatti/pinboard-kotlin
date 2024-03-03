package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.functional.Either
import com.fibelatti.pinboard.features.notes.domain.model.Note
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlin.reflect.KClass

sealed class Content

sealed class ContentWithHistory : Content() {

    abstract val previousContent: Content
}

interface ConnectionAwareContent {

    val isConnected: Boolean
}

/**
 * Marker interface to identify a [Content] that can be opened on a side panel
 * on foldable phones and tablets.
 */
interface SidePanelContent

data class LoginContent(
    val isUnauthorized: Boolean = false,
) : ContentWithHistory() {

    override val previousContent: Content = ExternalContent
}

data class PostListContent(
    val category: ViewCategory,
    val posts: PostList?,
    val showDescription: Boolean,
    val sortType: SortType,
    val searchParameters: SearchParameters,
    val shouldLoad: ShouldLoad,
    val canForceSync: Boolean = true,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent {

    override val previousContent: Content = ExternalContent

    val totalCount: Int
        get() = posts?.totalCount ?: 0
    val currentCount: Int
        get() = posts?.list?.size ?: 0
    val currentList: List<Post>
        get() = posts?.list ?: emptyList()
}

inline fun <reified T : Content> Content.find(): T? = find(T::class)

@Suppress("UNCHECKED_CAST")
fun <T : Content> Content.find(type: KClass<T>): T? = when {
    type.isInstance(this) -> this as? T
    this !is ContentWithHistory -> null
    else -> previousContent.find(type)
}

data class PostDetailContent(
    val post: Post,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

data class ExternalBrowserContent(
    val post: Post,
    override val previousContent: Content,
) : ContentWithHistory()

data class SearchContent(
    val searchParameters: SearchParameters,
    val availableTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val shouldLoadTags: Boolean = true,
    override val previousContent: PostListContent,
) : ContentWithHistory()

data class AddPostContent(
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val defaultTags: List<Tag>,
    override val previousContent: PostListContent,
) : ContentWithHistory()

data class EditPostContent(
    val post: Post,
    override val previousContent: Content,
) : ContentWithHistory()

data class TagListContent(
    val tags: List<Tag>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

data class SavedFiltersContent(
    override val previousContent: PostListContent,
) : ContentWithHistory()

data class NoteListContent(
    val notes: List<Note>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

data class NoteDetailContent(
    val id: String,
    val note: Either<Boolean, Note>,
    override val previousContent: NoteListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

data class PopularPostsContent(
    val posts: List<Post>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

data class PopularPostDetailContent(
    val post: Post,
    override val previousContent: PopularPostsContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

data class UserPreferencesContent(
    override val previousContent: PostListContent,
) : ContentWithHistory()

/**
 * Used when sharing URLs to the app. It usually indicates that the app should be finished so that
 * the user can return to the origin of the deeplink.
 */
data object ExternalContent : Content()
