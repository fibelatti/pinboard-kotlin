package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.extension.toResult
import com.fibelatti.pinboard.core.network.retryIO
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import retrofit2.Response
import retrofit2.http.GET
import javax.inject.Inject

class TagsDataSource @Inject constructor(
    private val tagsApi: TagsApi
) : TagsRepository {

    override suspend fun getAllTags(): Result<Map<String, Int>> =
        retryIO { tagsApi.getTags() }
            .toResult()
            .mapCatching { it.mapValues { (_, value) -> value.toInt() } }
}

interface TagsApi {

    @GET("/tags/get")
    fun getTags(): Response<TagsDto>
}
