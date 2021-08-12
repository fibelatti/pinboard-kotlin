package com.fibelatti.pinboard.features.tags.data

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.tags.domain.TagsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TagsDataSource @Inject constructor(
    private val tagsApi: TagsApi,
    private val postsDao: PostsDao,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : TagsRepository {

    override suspend fun getAllTags(): Result<List<Tag>> = withContext(Dispatchers.IO) {
        if (connectivityInfoProvider.isConnected()) {
            resultFromNetwork { tagsApi.getTags() }
                .mapCatching { tagsAndPostCount ->
                    tagsAndPostCount.map { (tag, postCount) -> Tag(tag, postCount.toInt()) }
                }
        } else {
            resultFrom { postsDao.getAllPostTags() }
                .mapCatching { concatenatedTags ->
                    concatenatedTags
                        .flatMap { it.split(" ") }
                        .groupBy { it }
                        .map { (tag, postList) -> Tag(tag, postList.size) }
                }
        }
    }
}
