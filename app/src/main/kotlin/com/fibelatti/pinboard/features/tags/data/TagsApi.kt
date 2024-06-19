package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.pinboard.core.di.RestApi
import com.fibelatti.pinboard.core.di.RestApiProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import org.koin.core.annotation.ScopeId
import javax.inject.Inject

@Factory
class TagsApi @Inject constructor(
    @ScopeId(name = "pinboard") @RestApi(RestApiProvider.PINBOARD) private val httpClient: HttpClient,
) {

    suspend fun getTags(): Map<String, Int> = httpClient.get(urlString = "tags/get").body()

    suspend fun renameTag(
        oldName: String,
        newName: String,
    ): RenameTagResponseDto = httpClient.get(urlString = "tags/rename") {
        url {
            parameters.append(name = "old", value = oldName)
            parameters.append(name = "new", value = newName)
        }
    }.body()
}

@Serializable
class RenameTagResponseDto(val result: String)
