package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.core.functional.onSuccess
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
        localTags?.let { value -> emit(Success(value)) }
        emit(getLocalTags())
        if (connectivityInfoProvider.isConnected()) {
            emit(getRemoteTags())
        }
    }.onEach { result ->
        result.onSuccess { value -> localTags = value }
    }

    private suspend fun getLocalTags(): Result<List<Tag>> = resultFrom { postsDao.getAllPostTags() }
        .mapCatching { concatenatedTags ->
            concatenatedTags
                .flatMap { it.split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedBy { it.name }
        }
        .onFailureReturn(localTags.orEmpty())

    private suspend fun getRemoteTags(): Result<List<Tag>> = resultFromNetwork { tagsApi.getTags() }
        .mapCatching { tagsAndPostCount ->
            tagsAndPostCount
                .map { (tag, postCount) -> Tag(tag, postCount) }
                .sortedBy { it.name }
        }
        .onFailureReturn(localTags.orEmpty())

    override suspend fun renameTag(
        oldName: String,
        newName: String,
    ): Result<List<Tag>> = resultFromNetwork {
        tagsApi.renameTag(oldName = oldName, newName = newName)
    }.map { response ->
        if (response.result == ApiResultCodes.DONE.code) {
            localTags = localTags?.map { tag -> if (tag.name == oldName) tag.copy(name = newName) else tag }
            localTags?.let(::Success) ?: getRemoteTags()
        } else {
            Failure(ApiException(response.result))
        }
    }
}
