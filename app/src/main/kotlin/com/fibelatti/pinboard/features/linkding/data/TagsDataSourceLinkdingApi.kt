package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onFailure
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
        emit(getLocalTags())
        if (connectivityInfoProvider.isConnected()) {
            emitAll(getRemoteTags().map(::Success))
        }
    }.onEach { result -> result.getOrNull()?.let { localTags = it } }

    private suspend fun getLocalTags(): Result<List<Tag>> = localTags?.let(::Success)
        ?: resultFrom { bookmarksDao.getAllBookmarkTags() }.mapCatching { concatenatedTags ->
            concatenatedTags
                .flatMap { it.split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedBy { it.name }
        }

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
        // Linkding doesn't support renaming tags
        return Success(emptyList())
    }
}
