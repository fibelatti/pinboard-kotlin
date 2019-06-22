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
    val shouldLoad: Boolean
) : Content()

sealed class ContentWithHistory(
    open val previousContent: PostList
) : Content()

class PostDetail(
    val post: Post,
    previousContent: PostList
) : ContentWithHistory(previousContent)

data class SearchView(
    val searchParameters: SearchParameters,
    val availableTags: List<Tag> = emptyList(),
    val allTags: List<Tag> = emptyList(),
    val shouldLoadTags: Boolean = true,
    override val previousContent: PostList
) : ContentWithHistory(previousContent)

class AddPostView(
    previousContent: PostList
) : ContentWithHistory(previousContent)
