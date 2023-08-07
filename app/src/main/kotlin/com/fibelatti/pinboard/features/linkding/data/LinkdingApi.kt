package com.fibelatti.pinboard.features.linkding.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LinkdingApi {

    @GET("api/bookmarks/")
    suspend fun getBookmarks(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PaginatedResponseRemote<BookmarkRemote>

    @GET("api/bookmarks/{id}/")
    suspend fun getBookmark(
        @Path("id") id: String,
    ): BookmarkRemote

    @POST("api/bookmarks/")
    suspend fun createBookmark(
        @Body bookmarkRemote: BookmarkRemote,
    ): BookmarkRemote

    @PUT("api/bookmarks/{id}/")
    suspend fun updateBookmark(
        @Path("id") id: String,
        @Body bookmarkRemote: BookmarkRemote,
    ): BookmarkRemote

    @DELETE("api/bookmarks/{id}/")
    suspend fun deleteBookmark(
        @Path("id") id: String,
    ): Response<Unit>

    @GET("api/bookmarks/archived/")
    suspend fun getArchivedBookmarks(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PaginatedResponseRemote<BookmarkRemote>

    @POST("api/bookmarks/{id}/archive/")
    suspend fun archiveBookmark(
        @Path("id") id: String,
    ): Response<Unit>

    @POST("api/bookmarks/{id}/unarchive/")
    suspend fun unarchiveBookmark(
        @Path("id") id: String,
    ): Response<Unit>

    @GET("api/tags/")
    suspend fun getTags(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PaginatedResponseRemote<TagRemote>
}
