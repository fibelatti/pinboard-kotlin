package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppConfig.API_MAX_LENGTH
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
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
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val rateLimitRunner: RateLimitRunner
) : PostsRepository {

    override suspend fun update(): Result<String> {
        return resultFrom {
            rateLimitRunner.run {
                withContext(Dispatchers.IO) {
                    postsApi.update()
                }
            }
        }.mapCatching(UpdateDto::updateTime)
    }

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?
    ): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsApi.add(
                    url = url,
                    title = title.take(API_MAX_LENGTH),
                    description = description,
                    public = private?.let { if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES },
                    readLater = readLater?.let { if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO },
                    tags = tags?.joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST) { it.name }
                        ?.take(API_MAX_LENGTH)
                )
            }
        }.throwErrorIfNotDone()
    }

    override suspend fun delete(url: String): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsApi.delete(url)
            }
        }.throwErrorIfNotDone()
    }

    override suspend fun getAllPosts(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int
    ): Result<Pair<Int, List<Post>>?> {
        val isConnected = connectivityInfoProvider.isConnected()
        val localData = suspend {
            getLocalData(
                newestFirst,
                searchTerm,
                tags,
                untaggedOnly,
                publicPostsOnly,
                privatePostsOnly,
                readLaterOnly,
                countLimit,
                pageLimit,
                pageOffset
            )
        }

        if (!isConnected) {
            return localData()
        }

        val userLastUpdate = userRepository.getLastUpdate().takeIf { it.isNotBlank() }
        val apiLastUpdate = update().getOrDefault(dateFormatter.nowAsTzFormat())

        if (userLastUpdate != null && userLastUpdate == apiLastUpdate) {
            return localData()
        }

        return resultFrom {
            rateLimitRunner.run {
                withContext(Dispatchers.IO) {
                    postsApi.getAllPosts()
                }
            }
        }.mapCatching { posts ->
            withContext(Dispatchers.IO) {
                postsDao.deleteAllPosts()
                postsDao.savePosts(posts)
            }
        }.map {
            localData()
        }.onSuccess {
            userRepository.setLastUpdate(apiLastUpdate)
        }
    }

    @VisibleForTesting
    suspend fun getLocalDataSize(
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        countLimit: Int
    ): Int = withContext(Dispatchers.IO) {
        postsDao.getPostCount(
            term = PostsDao.preFormatTerm(searchTerm),
            tag1 = tags.getAndFormat(0),
            tag2 = tags.getAndFormat(1),
            tag3 = tags.getAndFormat(2),
            untaggedOnly = untaggedOnly,
            publicPostsOnly = publicPostsOnly,
            privatePostsOnly = privatePostsOnly,
            readLaterOnly = readLaterOnly,
            limit = countLimit
        )
    }

    @VisibleForTesting
    suspend fun getLocalData(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int
    ): Result<Pair<Int, List<Post>>?> {
        return resultFrom {
            val localDataSize = getLocalDataSize(
                searchTerm,
                tags,
                untaggedOnly,
                publicPostsOnly,
                privatePostsOnly,
                readLaterOnly,
                countLimit
            )

            if (localDataSize > 0) {
                val localData = withContext(Dispatchers.IO) {
                    postsDao.getAllPosts(
                        newestFirst = newestFirst,
                        term = PostsDao.preFormatTerm(searchTerm),
                        tag1 = tags.getAndFormat(0),
                        tag2 = tags.getAndFormat(1),
                        tag3 = tags.getAndFormat(2),
                        untaggedOnly = untaggedOnly,
                        publicPostsOnly = publicPostsOnly,
                        privatePostsOnly = privatePostsOnly,
                        readLaterOnly = readLaterOnly,
                        limit = pageLimit,
                        offset = pageOffset
                    )
                }.let(postDtoMapper::mapList)

                localDataSize to localData
            } else {
                null
            }
        }
    }

    private fun List<Tag>?.getAndFormat(index: Int): String {
        return this?.getOrNull(index)?.name?.let(PostsDao.Companion::preFormatTag).orEmpty()
    }

    override suspend fun getPost(url: String): Result<Post> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsApi.getPost(url)
            }.posts.first()
        }.mapCatching(postDtoMapper::map)
    }

    override suspend fun searchExistingPostTag(tag: String): Result<List<String>> {
        return resultFrom {
            val concatenatedTags = withContext(Dispatchers.IO) {
                postsDao.searchExistingPostTag(PostsDao.preFormatTag(tag))
            }

            concatenatedTags.flatMap { it.split(" ") }
                .filter { it.startsWith(tag) }
                .distinct()
                .sorted()
        }
    }

    override suspend fun getSuggestedTagsForUrl(url: String): Result<SuggestedTags> {
        return resultFrom {
            rateLimitRunner.run {
                withContext(Dispatchers.IO) {
                    postsApi.getSuggestedTagsForUrl(url)
                }
            }
        }.mapCatching(suggestedTagDtoMapper::map)
    }

    override suspend fun clearCache(): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.deleteAllPosts()
            }
        }
    }

    private fun Result<GenericResponseDto>.throwErrorIfNotDone() =
        mapCatching {
            if (it.resultCode != ApiResultCodes.DONE.code) throw ApiException()
        }
}
