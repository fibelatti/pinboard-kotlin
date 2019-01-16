package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.extension.toResult
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagsDto
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

class PostsDataSource @Inject constructor(
    private val postsApi: PostsApi,
    private val postDtoMapper: PostDtoMapper,
    private val suggestedTagDtoMapper: SuggestedTagDtoMapper
) : PostsRepository {

    override suspend fun update(): Result<String> =
        postsApi.update()
            .toResult()
            .mapCatching { it.updateTime }

    override suspend fun add(
        url: String,
        description: String,
        extended: String?,
        tags: String?
    ): Result<Unit> =
        validateUrl(url) {
            postsApi.add(url, description, extended, tags)
                .toResult()
                .orThrow()
        }

    override suspend fun delete(
        url: String
    ): Result<Unit> =
        validateUrl(url) {
            postsApi.delete(url)
                .toResult()
                .orThrow()
        }

    override suspend fun getRecentPosts(
        tag: String?
    ): Result<List<Post>> =
        postsApi.getRecentPosts(tag)
            .toResult()
            .mapCatching(postDtoMapper::mapList)

    override suspend fun getAllPosts(
        tag: String?
    ): Result<List<Post>> =
        postsApi.getAllPosts(tag)
            .toResult()
            .mapCatching(postDtoMapper::mapList)

    override suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags> =
        postsApi.getSuggestedTagsForUrl(url)
            .toResult()
            .mapCatching(suggestedTagDtoMapper::map)

    private fun Result<GenericResponseDto>.orThrow() = mapCatching {
        if (it.resultCode != ApiResultCodes.DONE.code) throw InvalidRequestException()
    }

    private inline fun validateUrl(url: String, ifValid: () -> Result<Unit>): Result<Unit> {
        return if (url.substringBefore("://", "") !in UrlValidSchemes.allSchemes()) {
            Failure(InvalidRequestException())
        } else {
            ifValid()
        }
    }
}

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
    ): Response<List<PostDto>>

    @GET("/posts/all")
    fun getAllPosts(
        @Query("tag") tag: String? = null
    ): Response<List<PostDto>>

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
    FILE("file://");

    companion object {
        @JvmStatic
        fun allSchemes() = listOf(
            HTTP.scheme,
            HTTPS.scheme,
            JAVASCRIPT.scheme,
            MAILTO.scheme,
            FTP.scheme,
            FILE.scheme
        )
    }
}
