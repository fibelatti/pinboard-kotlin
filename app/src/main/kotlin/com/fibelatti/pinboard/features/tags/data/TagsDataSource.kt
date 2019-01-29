package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class TagsDataSource @Inject constructor(
    private val tagsApi: TagsApi
) : TagsRepository {

    override suspend fun getAllTags(): Result<Map<String, Int>> = withContext(Dispatchers.IO) {
        resultFrom { tagsApi.getTags().await() }
            .mapCatching { it.mapValues { (_, value) -> value.toInt() } }
    }
}

interface TagsApi {

    @GET("tags/get")
    fun getTags(): Deferred<TagsDto>
}
