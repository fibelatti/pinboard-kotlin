package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class TagsDataSource @Inject constructor(
    private val tagsApi: TagsApi,
    private val rateLimitRunner: RateLimitRunner
) : TagsRepository {

    override suspend fun getAllTags(): Result<List<Tag>> =
        withContext(Dispatchers.IO) {
            resultFrom { rateLimitRunner.run(tagsApi::getTags) }
                .mapCatching { it.map { (tag, posts) -> Tag(tag, posts.toInt()) } }
        }
}

interface TagsApi {

    @GET("tags/get")
    suspend fun getTags(): TagsDto
}
