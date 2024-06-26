package com.fibelatti.bookmarking.features.tags.data

import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.core.network.ApiException
import com.fibelatti.bookmarking.core.network.ConnectivityInfoProvider
import com.fibelatti.bookmarking.core.network.PinboardApiResultCode
import com.fibelatti.bookmarking.core.network.resultFromNetwork
import com.fibelatti.bookmarking.features.tags.domain.TagsRepository
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import com.fibelatti.bookmarking.pinboard.data.TagsApi
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.resultFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single

@Single(binds = [])
internal class TagsDataSource(
    private val tagsApi: TagsApi,
    private val postsDao: PostsDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val appModeProvider: AppModeProvider,
) : TagsRepository {

    private var localTags: List<Tag>? = null

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        emit(getLocalTags())
        if (AppMode.PINBOARD == appModeProvider.appMode.value && connectivityInfoProvider.isConnected()) {
            emit(getRemoteTags())
        }
    }.onEach { result -> result.getOrNull()?.let { localTags = it } }

    private suspend fun getLocalTags(): Result<List<Tag>> = localTags?.let(::Success)
        ?: resultFrom { postsDao.getAllPostTags() }.mapCatching { concatenatedTags ->
            concatenatedTags
                .flatMap { it.split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedBy { it.name }
        }

    private suspend fun getRemoteTags(): Result<List<Tag>> = resultFromNetwork {
        tagsApi.getTags()
    }.mapCatching { tagsAndPostCount ->
        tagsAndPostCount
            .map { (tag, postCount) -> Tag(tag, postCount) }
            .sortedBy { it.name }
    }

    override suspend fun renameTag(
        oldName: String,
        newName: String,
    ): Result<List<Tag>> = resultFromNetwork {
        tagsApi.renameTag(oldName = oldName, newName = newName)
    }.map { response ->
        if (response.result == PinboardApiResultCode.DONE.value) {
            localTags = localTags?.map { tag -> if (tag.name == oldName) tag.copy(name = newName) else tag }
            localTags?.let(::Success) ?: getRemoteTags()
        } else {
            Failure(ApiException(response.result))
        }
    }
}
