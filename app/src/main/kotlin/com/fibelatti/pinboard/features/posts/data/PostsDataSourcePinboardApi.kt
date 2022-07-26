package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.AppConfig.API_BASE_URL_LENGTH
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.AppConfig.PinboardApiMaxLength
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.di.IoScope
import com.fibelatti.pinboard.core.extension.containsHtmlChars
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.network.RateLimitRunner
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class PostsDataSourcePinboardApi @Inject constructor(
    private val userRepository: UserRepository,
    private val postsApi: PostsApi,
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val postRemoteDtoMapper: PostRemoteDtoMapper,
    private val dateFormatter: DateFormatter,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
    private val rateLimitRunner: RateLimitRunner,
    @IoScope private val pagedRequestsScope: CoroutineScope,
) : PostsRepository {

    companion object {

        private const val HTTP_URI_TOO_LONG = 414

        private const val SERVER_DOWN_TIMEOUT_SHORT = 10_000L
        private const val SERVER_DOWN_TIMEOUT_LONG = 15_000L
    }

    override suspend fun update(): Result<String> = resultFromNetwork {
        withContext(Dispatchers.IO) {
            withTimeout(SERVER_DOWN_TIMEOUT_SHORT) {
                postsApi.update().updateTime
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
        replace: Boolean,
    ): Result<Post> = if (connectivityInfoProvider.isConnected()) {
        addPostRemote(url, title, description, private, readLater, tags, replace)
    } else {
        addPostLocal(url, title, description, private, readLater, tags)
    }

    private suspend fun addPostRemote(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
        replace: Boolean,
    ): Result<Post> {
        val trimmedTitle = title.take(PinboardApiMaxLength.TEXT_TYPE.value)
        val publicLiteral = private?.let { if (private) PinboardApiLiterals.NO else PinboardApiLiterals.YES }
        val readLaterLiteral = readLater?.let { if (readLater) PinboardApiLiterals.YES else PinboardApiLiterals.NO }
        val trimmedTags = tags.orEmpty().joinToString(PinboardApiLiterals.TAG_SEPARATOR_REQUEST) {
            it.name.replace(oldValue = "+", newValue = "%2b")
        }.take(PinboardApiMaxLength.TEXT_TYPE.value)
        val replaceLiteral = if (replace) PinboardApiLiterals.YES else PinboardApiLiterals.NO

        // The API abuses GET, this aims to avoid getting 414 errors
        val currentLength = API_BASE_URL_LENGTH - url.length -
            trimmedTitle.length - (publicLiteral?.length ?: 0) - (readLaterLiteral?.length ?: 0) -
            trimmedTags.length - replaceLiteral.length

        val add: suspend (Int) -> GenericResponseDto = { descriptionLength: Int ->
            postsApi.add(
                url = url,
                title = trimmedTitle,
                description = description?.take(descriptionLength),
                public = publicLiteral,
                readLater = readLaterLiteral,
                tags = trimmedTags,
                replace = replaceLiteral,
            )
        }

        return resultFromNetwork {
            val result = withContext(Dispatchers.IO) {
                withTimeout(SERVER_DOWN_TIMEOUT_LONG) {
                    try {
                        add(PinboardApiMaxLength.URI.value - currentLength)
                    } catch (httpException: HttpException) {
                        if (httpException.code() == HTTP_URI_TOO_LONG) {
                            add(PinboardApiMaxLength.SAFE_URI.value - currentLength)
                        } else {
                            throw httpException
                        }
                    }
                }
            }

            when (result.resultCode) {
                ApiResultCodes.DONE.code -> {
                    postsDao.deletePendingSyncPost(url)
                    postsApi.getPost(url).posts
                        .let(postRemoteDtoMapper::mapList)
                        .also { postsDao.savePosts(it) }
                        .first().let(postDtoMapper::map)
                }
                ApiResultCodes.ITEM_ALREADY_EXISTS.code -> getPost(url).getOrThrow()
                else -> throw ApiException()
            }
        }
    }

    private suspend fun addPostLocal(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
    ): Result<Post> = resultFrom {
        val existingPost = postsDao.getPost(url)

        val newPost = PostDto(
            href = existingPost?.href ?: url,
            description = title,
            extended = description,
            hash = existingPost?.hash ?: UUID.randomUUID().toString(),
            time = existingPost?.time ?: dateFormatter.nowAsTzFormat(),
            shared = if (private == true) PinboardApiLiterals.NO else PinboardApiLiterals.YES,
            toread = if (readLater == true) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = tags.orEmpty().joinToString(PinboardApiLiterals.TAG_SEPARATOR_RESPONSE) { it.name },
            pendingSync = existingPost?.let { it.pendingSync ?: PendingSyncDto.UPDATE } ?: PendingSyncDto.ADD,
        )

        postsDao.savePosts(listOf(newPost))

        return@resultFrom postDtoMapper.map(newPost)
    }

    override suspend fun delete(url: String): Result<Unit> = if (connectivityInfoProvider.isConnected()) {
        deletePostRemote(url)
    } else {
        deletePostLocal(url)
    }

    private suspend fun deletePostRemote(url: String): Result<Unit> = resultFromNetwork {
        withContext(Dispatchers.IO) { postsApi.delete(url) }
    }.mapCatching { result ->
        if (result.resultCode == ApiResultCodes.DONE.code) {
            postsDao.deletePost(url)
        } else {
            throw ApiException()
        }
    }

    private suspend fun deletePostLocal(url: String): Result<Unit> = resultFrom {
        val existingPost = postsDao.getPost(url)
            ?: throw IllegalStateException("Can't delete post with url: $url. It doesn't exist locally.")

        postsDao.savePosts(listOf(existingPost.copy(pendingSync = PendingSyncDto.DELETE)))
    }

    override suspend fun getAllPosts(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int,
        forceRefresh: Boolean,
    ): Flow<Result<PostListResult>> = flow {
        val localData: suspend (upToDate: Boolean) -> Result<PostListResult> = { upToDate: Boolean ->
            getLocalData(
                newestFirst,
                searchTerm,
                tags,
                untaggedOnly,
                postVisibility,
                readLaterOnly,
                countLimit,
                pageLimit,
                pageOffset,
                upToDate
            )
        }

        if (!connectivityInfoProvider.isConnected()) {
            emit(localData(true))
            return@flow
        }

        emit(localData(false))

        val userLastUpdate = userRepository.lastUpdate.takeIf(String::isNotBlank)
        val apiLastUpdate = update().getOrDefault(dateFormatter.nowAsTzFormat())

        if (userLastUpdate != null && userLastUpdate == apiLastUpdate && !forceRefresh) {
            emit(localData(true))
        } else {
            getAllFromApi(localData, apiLastUpdate)
        }
    }

    private suspend fun FlowCollector<Result<PostListResult>>.getAllFromApi(
        localData: suspend (upToDate: Boolean) -> Result<PostListResult>,
        apiLastUpdate: String,
    ) {
        pagedRequestsScope.coroutineContext.cancelChildren()
        val apiData = suspend {
            resultFromNetwork {
                withContext(Dispatchers.IO) {
                    postsApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE)
                }
            }.mapCatching { posts ->
                postsDao.deleteAllSyncedPosts()
                savePosts(posts.let(postRemoteDtoMapper::mapList))

                userRepository.lastUpdate = apiLastUpdate

                getAdditionalPages(initialOffset = posts.size)
            }.let { localData(true) }
        }

        emit(apiData())
    }

    @VisibleForTesting
    fun getAdditionalPages(initialOffset: Int) {
        pagedRequestsScope.launch {
            runCatching {
                var currentOffset = initialOffset

                while (currentOffset != 0) {
                    val additionalPosts = rateLimitRunner.run {
                        postsApi.getAllPosts(offset = currentOffset, limit = API_PAGE_SIZE)
                    }

                    if (additionalPosts.isNotEmpty()) {
                        savePosts(additionalPosts.let(postRemoteDtoMapper::mapList))
                        currentOffset += additionalPosts.size
                    } else {
                        currentOffset = 0
                    }
                }
            }
        }
    }

    @VisibleForTesting
    suspend fun savePosts(posts: List<PostDto>) {
        val updatedPosts = posts.map { post ->
            if (post.tags.containsHtmlChars() || post.href.contains("%20")) {
                post.copy(
                    href = post.href.replace("%20", " "),
                    tags = post.tags.replaceHtmlChars(),
                )
            } else {
                post
            }
        }
        postsDao.savePosts(updatedPosts)
    }

    override suspend fun getQueryResultSize(
        searchTerm: String,
        tags: List<Tag>?,
    ): Int = catching {
        getLocalDataSize(
            searchTerm = searchTerm,
            tags = tags,
            untaggedOnly = false,
            postVisibility = PostVisibility.None,
            readLaterOnly = false,
            countLimit = -1,
        )
    }.getOrDefault(0)

    @VisibleForTesting
    suspend fun getLocalDataSize(
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        countLimit: Int,
    ): Int = postsDao.getPostCount(
        term = PostsDao.preFormatTerm(searchTerm),
        tag1 = tags.getAndFormat(0),
        tag2 = tags.getAndFormat(1),
        tag3 = tags.getAndFormat(2),
        untaggedOnly = untaggedOnly,
        ignoreVisibility = postVisibility is PostVisibility.None,
        publicPostsOnly = postVisibility is PostVisibility.Public,
        privatePostsOnly = postVisibility is PostVisibility.Private,
        readLaterOnly = readLaterOnly,
        limit = countLimit
    )

    @VisibleForTesting
    suspend fun getLocalData(
        newestFirst: Boolean,
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int,
        upToDate: Boolean,
    ): Result<PostListResult> = resultFrom {
        val localDataSize = getLocalDataSize(
            searchTerm,
            tags,
            untaggedOnly,
            postVisibility,
            readLaterOnly,
            countLimit
        )

        val localData = if (localDataSize > 0) {
            postsDao.getAllPosts(
                newestFirst = newestFirst,
                term = PostsDao.preFormatTerm(searchTerm),
                tag1 = tags.getAndFormat(0),
                tag2 = tags.getAndFormat(1),
                tag3 = tags.getAndFormat(2),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicPostsOnly = postVisibility is PostVisibility.Public,
                privatePostsOnly = postVisibility is PostVisibility.Private,
                readLaterOnly = readLaterOnly,
                limit = pageLimit,
                offset = pageOffset
            ).let(postDtoMapper::mapList)
        } else {
            emptyList()
        }

        PostListResult(totalCount = localDataSize, posts = localData, upToDate = upToDate)
    }

    private fun List<Tag>?.getAndFormat(index: Int): String =
        this?.getOrNull(index)?.name?.let(PostsDao.Companion::preFormatTag).orEmpty()

    override suspend fun getPost(url: String): Result<Post> = resultFromNetwork {
        val post = postsDao.getPost(url) ?: withContext(Dispatchers.IO) {
            postsApi.getPost(url).posts.let(postRemoteDtoMapper::mapList).firstOrNull()
        }

        post?.let(postDtoMapper::map) ?: throw InvalidRequestException()
    }

    override suspend fun searchExistingPostTag(tag: String): Result<List<String>> = resultFromNetwork {
        val concatenatedTags = postsDao.searchExistingPostTag(PostsDao.preFormatTag(tag))

        concatenatedTags.flatMap { it.replaceHtmlChars().split(" ") }
            .filter { it.startsWith(tag, ignoreCase = true) }
            .distinct()
            .sorted()
    }

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = resultFrom {
        postsDao.getPendingSyncPosts().let(postDtoMapper::mapList)
    }

    override suspend fun clearCache(): Result<Unit> = resultFrom {
        postsDao.deleteAllPosts()
    }
}
