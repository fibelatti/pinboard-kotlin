package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import javax.inject.Inject

class TagsDataSource @Inject constructor(private val postsDao: PostsDao) : TagsRepository {

    override suspend fun getAllTags(): Result<List<Tag>> =
        withContext(Dispatchers.IO) {
            resultFrom { postsDao.getAllPostTags() }
                .mapCatching { concatenatedTags ->
                    concatenatedTags.flatMap { it.split(" ") }
                        .distinct()
                        .map { tag -> Tag(tag) }
                }
        }
}

interface TagsApi {

    @GET("tags/get")
    suspend fun getTags(): TagsDto
}
