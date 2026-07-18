package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

internal class TagsDataSourceNoApi @Inject constructor(
    private val postsDao: PostsDao,
) : TagsRepository {

    private var localTags: List<Tag>? = null

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        localTags?.let { value -> emit(Result.success(value)) }
        emit(getLocalTags())
    }.onEach { result ->
        result.onSuccess { value -> localTags = value }
    }

    private suspend fun getLocalTags(): Result<List<Tag>> = resultFrom { postsDao.getAllPostTags() }
        .coMapCatching { concatenatedTags ->
            concatenatedTags
                .flatMap { it.split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedBy { it.name }
        }
        .recover { localTags.orEmpty() }

    override suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>> {
        return Result.failure(UnsupportedOperationException())
    }
}
