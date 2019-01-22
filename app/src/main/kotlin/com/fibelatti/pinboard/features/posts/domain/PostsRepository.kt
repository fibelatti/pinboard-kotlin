package com.fibelatti.pinboard.features.posts.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags

interface PostsRepository {

    suspend fun update(): Result<String>

    suspend fun add(
        url: String,
        description: String,
        extended: String? = null,
        tags: List<String>? = null
    ): Result<Unit>

    suspend fun delete(
        url: String
    ): Result<Unit>

    suspend fun getRecentPosts(
        tags: List<String>? = null
    ): Result<List<Post>>

    suspend fun getAllPosts(
        tags: List<String>? = null
    ): Result<List<Post>>

    suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags>
}
