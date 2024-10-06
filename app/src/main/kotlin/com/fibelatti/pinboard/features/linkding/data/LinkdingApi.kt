package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject

internal class LinkdingApi @Inject constructor(
    @RestApi(RestApiProvider.LINKDING) private val httpClient: HttpClient,
) {

    suspend fun getBookmarks(
        offset: Int? = null,
        limit: Int? = null,
    ): PaginatedResponseRemote<BookmarkRemote> = httpClient.get(urlString = "api/bookmarks/") {
        url {
            offset?.let { parameters.append(name = "offset", value = "$it") }
            limit?.let { parameters.append(name = "limit", value = "$it") }
        }
    }.body()

    suspend fun getBookmark(
        id: String,
    ): BookmarkRemote = httpClient.get(urlString = "api/bookmarks/$id/").body()

    suspend fun createBookmark(
        bookmarkRemote: BookmarkRemote,
    ): BookmarkRemote = httpClient.post(urlString = "api/bookmarks/") {
        contentType(ContentType.Application.Json)
        setBody(bookmarkRemote)
    }.body()

    suspend fun updateBookmark(
        id: String,
        bookmarkRemote: BookmarkRemote,
    ): BookmarkRemote = httpClient.put(urlString = "api/bookmarks/$id/") {
        contentType(ContentType.Application.Json)
        setBody(bookmarkRemote)
    }.body()

    suspend fun deleteBookmark(
        id: String,
    ): Boolean = httpClient.delete(urlString = "api/bookmarks/$id/").status.isSuccess()

    suspend fun getArchivedBookmarks(
        offset: Int? = null,
        limit: Int? = null,
    ): PaginatedResponseRemote<BookmarkRemote> = httpClient.get(urlString = "api/bookmarks/archived/") {
        url {
            offset?.let { parameters.append(name = "offset", value = "$it") }
            limit?.let { parameters.append(name = "limit", value = "$it") }
        }
    }.body()

    suspend fun archiveBookmark(
        id: String,
    ): Boolean = httpClient.post(urlString = "api/bookmarks/$id/archive/").status.isSuccess()

    suspend fun unarchiveBookmark(
        id: String,
    ): Boolean = httpClient.post(urlString = "api/bookmarks/$id/unarchive/").status.isSuccess()

    suspend fun getTags(
        offset: Int? = null,
        limit: Int? = null,
    ): PaginatedResponseRemote<TagRemote> = httpClient.get(urlString = "api/tags/") {
        url {
            offset?.let { parameters.append(name = "offset", value = "$it") }
            limit?.let { parameters.append(name = "limit", value = "$it") }
        }
    }.body()
}
