package com.fibelatti.bookmarking.pinboard.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
internal class PostsApi(
    @Named("pinboard") private val httpClient: HttpClient,
) {

    suspend fun update(): UpdateDto = httpClient.get(urlString = "posts/update").body()

    suspend fun add(
        url: String,
        title: String,
        description: String? = null,
        public: String? = null,
        readLater: String? = null,
        tags: String? = null,
        replace: String? = null,
    ): GenericResponseDto = httpClient.get(urlString = "posts/add") {
        url {
            parameters.append(name = "url", value = url)
            parameters.append(name = "description", value = title)
            description?.let { parameters.append(name = "extended", value = description) }
            public?.let { parameters.append(name = "shared", value = public) }
            readLater?.let { parameters.append(name = "toread", value = readLater) }
            tags?.let { parameters.append(name = "tags", value = tags) }
            replace?.let { parameters.append(name = "replace", value = replace) }
        }
    }.body()

    suspend fun delete(url: String): GenericResponseDto = httpClient.get(urlString = "posts/delete") {
        url {
            parameters.append(name = "url", value = url)
        }
    }.body()

    suspend fun getPost(url: String): GetPostDto = httpClient.get(urlString = "posts/get") {
        url {
            parameters.append(name = "url", value = url)
        }
    }.body()

    suspend fun getAllPosts(
        offset: Int? = null,
        limit: Int? = null,
    ): List<PostRemoteDto> = httpClient.get(urlString = "posts/all") {
        url {
            offset?.let { parameters.append(name = "start", value = "$offset") }
            limit?.let { parameters.append(name = "results", value = "$limit") }
        }
    }.body()
}
