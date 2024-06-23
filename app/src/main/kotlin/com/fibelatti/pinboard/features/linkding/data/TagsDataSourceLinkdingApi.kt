package com.fibelatti.pinboard.features.linkding.data

import com.fibelatti.bookmarking.core.network.resultFromNetwork
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.resultFrom
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Factory

@Factory
class TagsDataSourceLinkdingApi(
    private val linkdingApi: LinkdingApi,
    private val bookmarksDao: BookmarksDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : TagsRepository {

    private var localTags: List<Tag>? = null

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        emit(getLocalTags())
        if (connectivityInfoProvider.isConnected()) {
            emit(getRemoteTags())
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

    private suspend fun getRemoteTags(): Result<List<Tag>> = resultFromNetwork { linkdingApi.getTags() }
        .mapCatching { paginatedResponse ->
            val localTags = getLocalTags().getOrNull()?.associateBy { it.name }.orEmpty()

            paginatedResponse.results.map {
                Tag(name = it.name, posts = localTags[it.name]?.posts ?: 0)
            }
        }

    override suspend fun renameTag(oldName: String, newName: String): Result<List<Tag>> {
        // Linkding doesn't support renaming tags
        return Success(emptyList())
    }
}
