package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

class PostsDataSource

interface PostsApi {

    @GET("/posts/update")
    fun update(): Response<UpdateDto>

    @GET("/posts/add")
    fun add(
        @Query("url") url: String,
        @Query("description") description: String,
        @Query("extended") extended: String? = null,
        @Query("tags") tags: String? = null
    ): Response<GenericResponseDto>

    @GET("/posts/delete")
    fun delete(
        @Query("url") url: String
    ): Response<GenericResponseDto>

    @GET("/posts/recent")
    fun getRecentPosts(
        @Query("tag") tag: String? = null
    ): Response<PostDto>

    @GET("/posts/all")
    fun getAllPosts(
        @Query("tag") tag: String? = null
    ): Response<PostDto>

    @GET("/posts/suggest")
    fun getSuggestedTagsForUrl(
        @Query("url") url: String
    ): Response<SuggestedTagsDto>
}

enum class UrlValidSchemes(val scheme: String) {
    HTTP("http://"),
    HTTPS("https://"),
    JAVASCRIPT("javascript://"),
    MAILTO("mailto://"),
    FTP("ftp://"),
    FILE("file://"),
}
