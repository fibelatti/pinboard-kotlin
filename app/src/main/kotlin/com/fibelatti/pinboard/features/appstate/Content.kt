package com.fibelatti.pinboard.features.appstate

import com.fibelatti.core.extension.orZero
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.presentation.PostListDiffUtil
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class Content

data class PostList(
    val category: ViewCategory,
    val title: String,
    val posts: Triple<Int, List<Post>, PostListDiffUtil>?,
    val sortType: SortType,
    val searchParameters: SearchParameters,
    val shouldLoad: ShouldLoad,
    val isConnected: Boolean = true
) : Content() {

    val currentCount: Int
        get() = posts?.second?.size.orZero()
    val currentList: List<Post>
        get() = posts?.second ?: emptyList()
}

private interface ContentHistory {
    val previousContent: Content
}

sealed class ContentWithHistory : Content(), ContentHistory

data class PostDetail(
    val post: Post,
    override val previousContent: PostList
) : ContentWithHistory()

data class SearchView(
    val searchParameters: SearchParameters,
    val availableTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val shouldLoadTags: Boolean = true,
    override val previousContent: PostList
) : ContentWithHistory()

data class AddPostView(
    override val previousContent: PostList
) : ContentWithHistory()

data class EditPostView(
    val post: Post,
    override val previousContent: PostDetail
) : ContentWithHistory()

data class TagList(
    val tags: List<Tag>,
    val shouldLoad: Boolean,
    val isConnected: Boolean = true,
    override val previousContent: PostList
) : ContentWithHistory()
