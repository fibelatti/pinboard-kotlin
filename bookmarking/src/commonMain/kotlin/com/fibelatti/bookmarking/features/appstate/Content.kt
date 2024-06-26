package com.fibelatti.bookmarking.features.appstate

import com.fibelatti.bookmarking.features.notes.domain.model.Note
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Either
import kotlin.reflect.KClass

public sealed class Content

public sealed class ContentWithHistory : Content() {

    public abstract val previousContent: Content
}

public interface ConnectionAwareContent {

    public val isConnected: Boolean
}

/**
 * Marker interface to identify a [Content] that can be opened on a side panel
 * on foldable phones and tablets.
 */
public interface SidePanelContent

public data class LoginContent(
    val isUnauthorized: Boolean = false,
) : ContentWithHistory() {

    override val previousContent: Content = ExternalContent
}

public data class PostListContent(
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

public inline fun <reified T : Content> Content.find(): T? = find(T::class)

@Suppress("UNCHECKED_CAST")
public fun <T : Content> Content.find(type: KClass<T>): T? = when {
    type.isInstance(this) -> this as? T
    this !is ContentWithHistory -> null
    else -> previousContent.find(type)
}

public data class PostDetailContent(
    val post: Post,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

public data class ExternalBrowserContent(
    val post: Post,
    override val previousContent: Content,
) : ContentWithHistory()

public data class SearchContent(
    val searchParameters: SearchParameters,
    val availableTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val shouldLoadTags: Boolean = true,
    override val previousContent: PostListContent,
) : ContentWithHistory()

public data class AddPostContent(
    val defaultPrivate: Boolean,
    val defaultReadLater: Boolean,
    val defaultTags: List<Tag>,
    override val previousContent: PostListContent,
) : ContentWithHistory()

public data class EditPostContent(
    val post: Post,
    override val previousContent: Content,
) : ContentWithHistory()

public data class TagListContent(
    val tags: List<Tag>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

public data class SavedFiltersContent(
    override val previousContent: PostListContent,
) : ContentWithHistory()

public data class NoteListContent(
    val notes: List<Note>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

public data class NoteDetailContent(
    val id: String,
    val note: Either<Boolean, Note>,
    override val previousContent: NoteListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

public data class PopularPostsContent(
    val posts: List<Post>,
    val shouldLoad: Boolean,
    override val previousContent: PostListContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent

public data class PopularPostDetailContent(
    val post: Post,
    override val previousContent: PopularPostsContent,
    override val isConnected: Boolean = true,
) : ContentWithHistory(), ConnectionAwareContent, SidePanelContent

public data class UserPreferencesContent(
    override val previousContent: PostListContent,
) : ContentWithHistory()

/**
 * Used when sharing URLs to the app. It usually indicates that the app should be finished so that
 * the user can return to the origin of the deeplink.
 */
public data object ExternalContent : Content()
