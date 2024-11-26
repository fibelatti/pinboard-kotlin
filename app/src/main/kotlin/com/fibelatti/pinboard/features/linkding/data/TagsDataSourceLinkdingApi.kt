package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onFailureReturn
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class TagsDataSourceLinkdingApi @Inject constructor(
    private val linkdingApi: LinkdingApi,
    private val bookmarksDao: BookmarksDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : TagsRepository {

    private var localTags: List<Tag>? = null

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        localTags?.let { value -> emit(Success(value)) }
        emit(getLocalTags())
        if (connectivityInfoProvider.isConnected()) {
            emitAll(getRemoteTags().map(::Success))
        }
    }.onEach { result ->
        result.onSuccess { value -> localTags = value }
    }

    private suspend fun getLocalTags(): Result<List<Tag>> = resultFrom { bookmarksDao.getAllBookmarkTags() }
        .mapCatching { concatenatedTags ->
            concatenatedTags
                .flatMap { it.split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedBy { it.name }
        }
        .onFailureReturn(localTags.orEmpty())

    private fun getRemoteTags(): Flow<List<Tag>> = flow {
        val localTags = getLocalTags().getOrNull()?.associateBy { it.name }.orEmpty()
        val remoteTags = mutableSetOf<Tag>()
        var currentPage: PaginatedResponseRemote<TagRemote>

        resultFromNetwork {
            do {
                currentPage = linkdingApi.getTags(offset = remoteTags.size, limit = 1_000)
                remoteTags += currentPage.results.map { tag ->
                    Tag(name = tag.name, posts = localTags[tag.name]?.posts ?: 0)
                }

                emit(remoteTags.toList())
            } while (currentPage.next != null)
        }.onFailure {
            emit(localTags.values.toList())
        }
    }

    override suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>> {
        return Failure(UnsupportedOperationException())
    }
}
