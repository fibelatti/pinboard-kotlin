package com.fibelatti.bookmarking.features.posts.domain

import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow

public interface PostsRepository {

    public suspend fun update(): Result<String>

    public suspend fun add(post: Post): Result<Post>

    public suspend fun delete(id: String, url: String): Result<Unit>

    public fun getAllPosts(
        sortType: SortType,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int,
        forceRefresh: Boolean,
    ): Flow<Result<PostListResult>>

    public suspend fun getQueryResultSize(
        searchTerm: String,
        tags: List<Tag>?,
    ): Int

    public suspend fun getPost(id: String, url: String): Result<Post>

    public suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag> = emptyList(),
    ): Result<List<String>>

    public suspend fun getPendingSyncPosts(): Result<List<Post>>

    public suspend fun clearCache(): Result<Unit>
}
