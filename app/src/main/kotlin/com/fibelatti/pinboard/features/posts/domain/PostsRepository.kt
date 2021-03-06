package com.fibelatti.pinboard.features.posts.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface PostsRepository {

    suspend fun update(): Result<String>

    suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
        replace: Boolean
    ): Result<Post>

    suspend fun delete(url: String): Result<Unit>

    suspend fun getAllPosts(
        newestFirst: Boolean,
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

    suspend fun getPost(url: String): Result<Post>

    suspend fun searchExistingPostTag(tag: String): Result<List<String>>

    suspend fun getSuggestedTagsForUrl(url: String): Result<SuggestedTags>

    suspend fun clearCache(): Result<Unit>
}
