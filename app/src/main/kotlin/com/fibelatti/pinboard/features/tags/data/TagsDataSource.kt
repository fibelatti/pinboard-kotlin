package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagsDataSource @Inject constructor(
    private val tagsApi: TagsApi,
    private val postsDao: PostsDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    @MainVariant private val mainVariant: Boolean,
) : TagsRepository {

    override fun getAllTags(): Flow<Result<List<Tag>>> = flow {
        emit(getLocalTags())
        if (mainVariant && connectivityInfoProvider.isConnected()) emit(getRemoteTags())
    }

    private suspend fun getLocalTags(): Result<List<Tag>> = resultFrom {
        postsDao.getAllPostTags()
    }.mapCatching { concatenatedTags ->
        concatenatedTags
            .flatMap { it.split(" ") }
            .groupBy { it }
            .map { (tag, postList) -> Tag(tag, postList.size) }
            .sortedBy { it.name }
    }

    private suspend fun getRemoteTags(): Result<List<Tag>> = withContext(Dispatchers.IO) {
        resultFromNetwork { tagsApi.getTags() }
    }.mapCatching { tagsAndPostCount ->
        tagsAndPostCount
            .map { (tag, postCount) -> Tag(tag, postCount.toInt()) }
            .sortedBy { it.name }
    }
}
