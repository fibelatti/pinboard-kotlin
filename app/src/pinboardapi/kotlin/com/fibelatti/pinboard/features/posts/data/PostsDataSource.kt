package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppConfig.API_GET_ALL_THROTTLE_TIME
import com.fibelatti.pinboard.core.AppConfig.API_MAX_EXTENDED_LENGTH
import com.fibelatti.pinboard.core.AppConfig.API_MAX_LENGTH
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.IoScope
import com.fibelatti.pinboard.core.extension.containsHtmlChars
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.SuggestedTagDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
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
    private val rateLimitRunner: RateLimitRunner,
    @IoScope private val pagedRequestsScope: CoroutineScope
) : PostsRepository {

    override suspend fun update(): Result<String> {
        return resultFrom {
            rateLimitRunner.run {
                withContext(Dispatchers.IO) {
                    postsApi.update().updateTime
                }
            }
        }
    }

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
        replace: Boolean
    ): Result<Post> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                val result = postsApi.add(
                    url = url,
                    title = title.take(API_MAX_LENGTH),
                    description = description?.take(API_MAX_EXTENDED_LENGTH),
                    public = private?.let { if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES },
                    readLater = readLater?.let { if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO },
                    tags = tags?.joinToString(
                        PinboardApiLiterals.TAG_SEPARATOR_REQUEST,
                        transform = Tag::name
                    )?.take(API_MAX_LENGTH),
                    replace = if (replace) PinboardApiLiterals.YES else PinboardApiLiterals.NO
                )

                when (result.resultCode) {
                    ApiResultCodes.DONE.code -> {
                        postsApi.getPost(url).posts.first().let(postDtoMapper::map)
                    }
                    ApiResultCodes.ITEM_ALREADY_EXISTS.code -> {
                        (postsDao.getPost(url) ?: postsApi.getPost(url).posts.firstOrNull())
                            ?.let(postDtoMapper::map)
                            ?: throw InvalidRequestException()
                    }
                    else -> throw ApiException()
                }
            }
        }
    }

    override suspend fun delete(url: String): Result<Unit> {
        return resultFrom {
            val result = withContext(Dispatchers.IO) {
                postsApi.delete(url)
            }

            if (result.resultCode != ApiResultCodes.DONE.code) {
                throw ApiException()
            }
        }
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
            pagedRequestsScope.coroutineContext.cancelChildren()

            rateLimitRunner.run(API_GET_ALL_THROTTLE_TIME) {
                withContext(Dispatchers.IO) {
                    postsApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE)
                }
            }
        }.mapCatching { posts ->
            withContext(Dispatchers.IO) {
                postsDao.deleteAllPosts()
                savePosts(posts)
            }

            if (posts.size == API_PAGE_SIZE) {
                getAdditionalPages()
            }
        }.map {
            localData()
        }.onSuccess {
            userRepository.setLastUpdate(apiLastUpdate)
        }
    }

    @VisibleForTesting
    fun getAdditionalPages() {
        pagedRequestsScope.launch {
            try {
                var currentOffset = API_PAGE_SIZE

                while (currentOffset != 0) {
                    val additionalPosts = rateLimitRunner.run(API_GET_ALL_THROTTLE_TIME) {
                        postsApi.getAllPosts(offset = currentOffset, limit = API_PAGE_SIZE)
                    }

                    savePosts(additionalPosts)

                    if (additionalPosts.size == API_PAGE_SIZE) {
                        currentOffset += additionalPosts.size
                    } else {
                        currentOffset = 0
                    }
                }
            } catch (ignored: Exception) {
                // If it fails it can be resumed later
            }
        }
    }

    @VisibleForTesting
    fun savePosts(posts: List<PostDto>) {
        val updatedPosts = posts.map { post ->
            if (post.tags.containsHtmlChars()) {
                post.copy(tags = post.tags.replaceHtmlChars())
            } else {
                post
            }
        }
        postsDao.savePosts(updatedPosts)
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
                postsDao.getPost(url) ?: postsApi.getPost(url).posts.firstOrNull()
            }?.let(postDtoMapper::map) ?: throw InvalidRequestException()
        }
    }

    override suspend fun searchExistingPostTag(tag: String): Result<List<String>> {
        return resultFrom {
            val concatenatedTags = withContext(Dispatchers.IO) {
                postsDao.searchExistingPostTag(PostsDao.preFormatTag(tag))
            }

            concatenatedTags.flatMap { it.replaceHtmlChars().split(" ") }
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
            }.let(suggestedTagDtoMapper::map)
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.deleteAllPosts()
            }
        }
    }
}
