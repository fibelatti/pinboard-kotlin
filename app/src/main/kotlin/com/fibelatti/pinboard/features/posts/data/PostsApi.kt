package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

interface PostsApi {

    @GET("posts/update")
    fun update(): Deferred<UpdateDto>

    @GET("posts/add")
    fun add(
        @Query("url") url: String,
        @Query("description") description: String,
        @Query("extended") extended: String? = null,
        @Query("tags") tags: String? = null
    ): Deferred<GenericResponseDto>

    @GET("posts/delete")
    fun delete(
        @Query("url") url: String
    ): Deferred<GenericResponseDto>

    @GET("posts/recent")
    fun getRecentPosts(
        @Query("tag") tag: String? = null
    ): Deferred<List<PostDto>>

    @GET("posts/all")
    fun getAllPosts(
        @Query("tag") tag: String? = null
    ): Deferred<List<PostDto>>

    @GET("posts/suggest")
    fun getSuggestedTagsForUrl(
        @Query("url") url: String
    ): Deferred<SuggestedTagsDto>
}
