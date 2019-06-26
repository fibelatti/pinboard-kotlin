package com.fibelatti.pinboard.features.posts.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag

interface PostsRepository {

    suspend fun update(): Result<String>

    suspend fun add(
        url: String,
        title: String,
        description: String? = null,
        private: Boolean? = null,
        readLater: Boolean? = null,
        tags: List<Tag>? = null
    ): Result<Unit>

    suspend fun delete(
        url: String
    ): Result<Unit>

    suspend fun getRecentPosts(
        tags: List<Tag>? = null
    ): Result<List<Post>>

    suspend fun getAllPosts(
        tags: List<Tag>? = null
    ): Result<List<Post>>

    suspend fun getPost(
        url: String
    ): Result<Post>

    suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags>
}
