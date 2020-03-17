package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.GetPostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PostsApi {

    @GET("posts/update")
    suspend fun update(): UpdateDto

    @GET("posts/add")
    suspend fun add(
        @Query("url") url: String,
        @Query("description") title: String,
        @Query("extended") description: String? = null,
        @Query("shared") public: String? = null,
        @Query("toread") readLater: String? = null,
        @Query("tags", encoded = true) tags: String? = null,
        @Query("replace") replace: String? = null
    ): GenericResponseDto

    @GET("posts/delete")
    suspend fun delete(@Query("url") url: String): GenericResponseDto

    @GET("posts/get")
    suspend fun getPost(@Query("url") url: String): GetPostDto

    @GET("posts/all")
    suspend fun getAllPosts(
        @Query("start") offset: Int? = null,
        @Query("results") limit: Int? = null
    ): List<PostDto>

    @GET("posts/suggest")
    suspend fun getSuggestedTagsForUrl(@Query("url") url: String): SuggestedTagsDto
}
