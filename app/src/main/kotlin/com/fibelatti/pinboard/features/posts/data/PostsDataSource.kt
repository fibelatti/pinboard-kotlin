package com.fibelatti.pinboard.features.posts.data

import android.net.ConnectivityManager
import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppConfig.API_MAX_LENGTH
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.extension.isConnected
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.RateLimitRunner
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
    private val connectivityManager: ConnectivityManager?,
    private val rateLimitRunner: RateLimitRunner
) : PostsRepository {

    override suspend fun update(): Result<String> = withContext(Dispatchers.IO) {
        resultFrom { rateLimitRunner.run(postsApi::update) }
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

    private fun List<Tag>.forRequest() = joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST) { it.name }

    override suspend fun delete(
        url: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        resultFrom { postsApi.delete(url) }
            .orThrow()
    }

    override suspend fun getAllPosts(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        limit: Int
    ): Result<Pair<Int, List<Post>>?> = withContext(Dispatchers.IO) {
        val isConnected = connectivityManager.isConnected()
        val localDataSize = getLocalDataSize(
            searchTerm,
            tags,
            untaggedOnly,
            publicPostsOnly,
            privatePostsOnly,
            readLaterOnly,
            limit
        )
        val localData by lazy {
            getLocalData(
                newestFirst,
                searchTerm,
                tags,
                untaggedOnly,
                publicPostsOnly,
                privatePostsOnly,
                readLaterOnly,
                limit
            )
        }

        when {
            !isConnected && localDataSize > 0 -> localData
            !isConnected -> Success(null)
            else -> {
                val userLastUpdate = userRepository.getLastUpdate()
                val apiLastUpdate = update().getOrDefault(dateFormatter.nowAsTzFormat())

                if (userLastUpdate == apiLastUpdate && localDataSize > 0) {
                    localData
                } else {
                    resultFrom { rateLimitRunner.run { postsApi.getAllPosts() } }
                        .mapCatching { posts ->
                            postsDao.deleteAllPosts()
                            postsDao.savePosts(posts)
                        }
                        .map { localData }
                        .onSuccess { userRepository.setLastUpdate(apiLastUpdate) }
                }
            }
        }
    }

    @VisibleForTesting
    fun getLocalDataSize(
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        limit: Int
    ): Int {
        return postsDao.getPostCount(
            term = searchTerm,
            tag1 = tags?.getOrNull(0)?.name.orEmpty(),
            tag2 = tags?.getOrNull(1)?.name.orEmpty(),
            tag3 = tags?.getOrNull(2)?.name.orEmpty(),
            untaggedOnly = untaggedOnly,
            publicPostsOnly = publicPostsOnly,
            privatePostsOnly = privatePostsOnly,
            readLaterOnly = readLaterOnly,
            limit = limit
        )
    }

    @VisibleForTesting
    fun getLocalData(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        limit: Int
    ): Result<Pair<Int, List<Post>>?> {
        return catching {
            val localDataSize = getLocalDataSize(
                searchTerm,
                tags,
                untaggedOnly,
                publicPostsOnly,
                privatePostsOnly,
                readLaterOnly,
                limit
            )

            if (localDataSize > 0) {
                localDataSize to postsDao.getAllPosts(
                    newestFirst = newestFirst,
                    term = searchTerm,
                    tag1 = tags?.getOrNull(0)?.name.orEmpty(),
                    tag2 = tags?.getOrNull(1)?.name.orEmpty(),
                    tag3 = tags?.getOrNull(2)?.name.orEmpty(),
                    untaggedOnly = untaggedOnly,
                    publicPostsOnly = publicPostsOnly,
                    privatePostsOnly = privatePostsOnly,
                    readLaterOnly = readLaterOnly,
                    limit = limit
                ).let(postDtoMapper::mapList)
            } else {
                null
            }
        }
    }

    override suspend fun getPost(url: String): Result<Post> = withContext(Dispatchers.IO) {
        resultFrom { postsApi.getPost(url) }
            .mapCatching { postDtoMapper.map(it.posts.first()) }
    }

    override suspend fun getSuggestedTagsForUrl(
        url: String
    ): Result<SuggestedTags> = withContext(Dispatchers.IO) {
        resultFrom { rateLimitRunner.run { postsApi.getSuggestedTagsForUrl(url) } }
            .mapCatching(suggestedTagDtoMapper::map)
    }

    private fun Result<GenericResponseDto>.orThrow() = mapCatching {
        if (it.resultCode != ApiResultCodes.DONE.code) throw ApiException()
    }
}
