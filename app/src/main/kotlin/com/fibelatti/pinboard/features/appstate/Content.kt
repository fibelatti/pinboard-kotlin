package com.fibelatti.pinboard.features.appstate

import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag

sealed class Content

data class PostList(
    val category: ViewCategory,
    val title: String,
    val posts: List<Post>,
    val sortType: SortType,
    val searchParameters: SearchParameters,
    val shouldLoad: Boolean,
    val isConnected: Boolean = true
) : Content()

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
