package com.fibelatti.pinboard.features.posts.domain

import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.appstate.SortType
import kotlinx.coroutines.flow.Flow

interface PostsRepository {

    suspend fun update(): Result<String>

    suspend fun add(post: Post): Result<Post>

    suspend fun delete(id: String, url: String): Result<Unit>

    fun getAllPosts(
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

    suspend fun getQueryResultSize(
        searchTerm: String,
        tags: List<Tag>?,
    ): Int

    suspend fun getPost(id: String, url: String): Result<Post>

    suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag> = emptyList(),
    ): Result<List<String>>

    suspend fun getPendingSyncPosts(): Result<List<Post>>

    suspend fun clearCache(): Result<Unit>
}
