package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

internal class TagsDataSourcePinboardApi @Inject constructor(
    private val tagsApi: TagsApi,
    private val postsDao: PostsDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : TagsRepository {

    private var localTags: List<Tag>? = null

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        localTags?.let { value -> emit(Result.success(value)) }
        emit(getLocalTags())
        if (connectivityInfoProvider.isConnected()) {
            emit(getRemoteTags())
        }
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

    private suspend fun getRemoteTags(): Result<List<Tag>> = resultFromNetwork { tagsApi.getTags() }
        .coMapCatching { tagsAndPostCount ->
            tagsAndPostCount
                .map { (tag, postCount) -> Tag(tag, postCount) }
                .sortedBy { it.name }
        }
        .recover { localTags.orEmpty() }

    override suspend fun renameTag(
        oldName: String,
        newName: String,
    ): Result<List<Tag>> = resultFromNetwork {
        tagsApi.renameTag(oldName = oldName, newName = newName)
    }.coMapCatching { response ->
        if (response.result == ApiResultCodes.DONE.code) {
            localTags = localTags?.map { tag -> if (tag.name == oldName) tag.copy(name = newName) else tag }
            localTags ?: getRemoteTags().getOrThrow()
        } else {
            throw ApiException(response.result)
        }
    }
}
