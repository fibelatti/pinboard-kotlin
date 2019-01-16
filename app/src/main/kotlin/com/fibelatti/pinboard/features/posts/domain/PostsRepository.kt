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
        tags: String? = null
    ): Result<Unit>

    suspend fun delete(
        url: String
    ): Result<Unit>

    suspend fun getRecentPosts(
        tag: String? = null
    ): Result<List<Post>>

    suspend fun getAllPosts(
        tag: String? = null
    ): Result<List<Post>>

    suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags>
}
