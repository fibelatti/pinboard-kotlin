package com.fibelatti.pinboard.features.posts.data

import android.net.ConnectivityManager
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppConfig.API_MAX_LENGTH
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.ApiResultCodes
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.UpdateDto
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostsDataSource @Inject constructor(
    private val userRepository: UserRepository,
    private val postsApi: PostsApi,
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val suggestedTagDtoMapper: SuggestedTagDtoMapper,
    private val dateFormatter: DateFormatter,
    private val connectivityManager: ConnectivityManager?
) : PostsRepository {

    override suspend fun update(): Result<String> = withContext(Dispatchers.IO) {
        resultFrom(postsApi::update)
            .mapCatching(UpdateDto::updateTime)
    }

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        resultFrom {
            postsApi.add(
                url = url,
                title = title.take(API_MAX_LENGTH),
                description = description,
                public = private?.let { if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES },
                readLater = readLater?.let { if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO },
                tags = tags?.forRequest()?.take(API_MAX_LENGTH)
            )
        }.orThrow()
    }

    override suspend fun delete(
        url: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        resultFrom { postsApi.delete(url) }
            .orThrow()
    }

    override suspend fun getRecentPosts(
        tags: List<Tag>?
    ): Result<List<Post>> = withContext(Dispatchers.IO) {
        resultFrom { postsApi.getRecentPosts(tags?.forRequest()) }
            .mapCatching { postDtoMapper.mapList(it.posts) }
    }

    override suspend fun getAllPosts(
        tags: List<Tag>?
    ): Result<List<Post>> = withContext(Dispatchers.IO) {
        val isConnected = connectivityManager.isConnected()
        val localPosts = catching { postsDao.getAllPosts() }
        val hasLocalData = localPosts.getOrDefault(emptyList()).isNotEmpty()

        if (!isConnected && hasLocalData) {
            localPosts
        } else {
            val userLastUpdate = userRepository.getLastUpdate()
            val apiLastUpdate = update().getOrNull() ?: dateFormatter.nowAsTzFormat()

            if (userLastUpdate == apiLastUpdate && hasLocalData) {
                localPosts
            } else {
                resultFrom { postsApi.getAllPosts(tags?.forRequest()) }
                    .mapCatching { posts ->
                        postsDao.deleteAllPosts()
                        postsDao.savePosts(posts)

                        posts
                    }
                    .onSuccess { userRepository.setLastUpdate(apiLastUpdate) }
            }
        }.mapCatching(postDtoMapper::mapList)
    }

    override suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags> = withContext(Dispatchers.IO) {
        resultFrom { postsApi.getSuggestedTagsForUrl(url) }
            .mapCatching(suggestedTagDtoMapper::map)
    }

    private fun Result<GenericResponseDto>.orThrow() = mapCatching {
        if (it.resultCode != ApiResultCodes.DONE.code) throw ApiException()
    }

    private fun List<Tag>.forRequest() = joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST) { it.name }
}
