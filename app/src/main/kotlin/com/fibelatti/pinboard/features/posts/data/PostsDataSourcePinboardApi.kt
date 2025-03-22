package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.getOrThrow
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.mapFailure
import com.fibelatti.pinboard.core.AppConfig.API_BASE_URL_LENGTH
import com.fibelatti.pinboard.core.AppConfig.API_PAGE_SIZE
import com.fibelatti.pinboard.core.AppConfig.MALFORMED_OBJECT_THRESHOLD
import com.fibelatti.pinboard.core.AppConfig.PinboardApiLiterals
import com.fibelatti.pinboard.core.AppConfig.PinboardApiMaxLength
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.extension.containsHtmlChars
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.ApiException
import com.fibelatti.pinboard.core.network.ApiResultCodes
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.core.persistence.database.isFtsCompatible
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.data.model.GenericResponseDto
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.data.model.PostRemoteDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import io.ktor.client.plugins.ResponseException
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException

internal class PostsDataSourcePinboardApi @Inject constructor(
    private val userRepository: UserRepository,
    private val postsApi: PostsApi,
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val postRemoteDtoMapper: PostRemoteDtoMapper,
    private val dateFormatter: DateFormatter,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : PostsRepository {

    companion object {

        private const val HTTP_URI_TOO_LONG = 414

        private const val SERVER_DOWN_TIMEOUT_SHORT = 10_000L
        private const val SERVER_DOWN_TIMEOUT_LONG = 15_000L
    }

    private var pagedRequestsJob: Job? = null

    private fun <T> Result<T>.mapApiRequestFailure(
        endpoint: String,
    ): Result<T> = mapFailure { throwable ->
        val mappedValue = if (throwable is ResponseException) {
            RuntimeException(
                "Network call to `$endpoint` failed. HTTP Code ${throwable.response.status.value}.",
                throwable,
            )
        } else {
            throwable
        }

        Failure(mappedValue)
    }

    override suspend fun update(): Result<String> = resultFromNetwork {
        withTimeout(SERVER_DOWN_TIMEOUT_SHORT) {
            postsApi.update().updateTime
        }
    }.mapApiRequestFailure(endpoint = "posts/update")

    override suspend fun add(post: Post): Result<Post> {
        val resolvedPost = post.copy(
            id = post.id.ifBlank { UUID.randomUUID().toString() },
            dateAdded = post.dateAdded.ifNullOrBlank { dateFormatter.nowAsDataFormat() },
        )

        return if (connectivityInfoProvider.isConnected()) {
            addPostRemote(post = resolvedPost)
        } else {
            addPostLocal(post = resolvedPost)
        }
    }

    private suspend fun addPostRemote(post: Post): Result<Post> {
        val trimmedTitle = post.title.take(PinboardApiMaxLength.TEXT_TYPE.value)
        val publicLiteral = post.private?.let { if (it) PinboardApiLiterals.NO else PinboardApiLiterals.YES }
        val readLaterLiteral = post.readLater?.let { if (it) PinboardApiLiterals.YES else PinboardApiLiterals.NO }
        val trimmedTags = post.tags.orEmpty()
            .joinToString(PinboardApiLiterals.TAG_SEPARATOR) { it.name }
            .take(PinboardApiMaxLength.TEXT_TYPE.value)
        val replaceLiteral = PinboardApiLiterals.YES

        // The API abuses GET, this aims to avoid getting 414 errors
        val currentLength = API_BASE_URL_LENGTH - post.url.length -
            trimmedTitle.length - (publicLiteral?.length ?: 0) - (readLaterLiteral?.length ?: 0) -
            trimmedTags.length - replaceLiteral.length

        val add: suspend (Int) -> GenericResponseDto = { descriptionLength: Int ->
            postsApi.add(
                url = post.url,
                title = trimmedTitle,
                description = post.description.ifEmpty { null }?.take(descriptionLength),
                public = publicLiteral,
                readLater = readLaterLiteral,
                tags = trimmedTags,
                replace = replaceLiteral,
            )
        }

        val networkResult = resultFromNetwork {
            withTimeout(SERVER_DOWN_TIMEOUT_LONG) {
                try {
                    add(PinboardApiMaxLength.URI.value - currentLength)
                } catch (httpException: ResponseException) {
                    if (HTTP_URI_TOO_LONG == httpException.response.status.value) {
                        add(PinboardApiMaxLength.SAFE_URI.value - currentLength)
                    } else {
                        throw httpException
                    }
                }
            }
        }

        return when (networkResult) {
            is Success -> {
                catching {
                    when (networkResult.value.resultCode) {
                        ApiResultCodes.DONE.code -> {
                            postsDao.deletePendingSyncPost(post.url)
                            savePosts(listOf(postDtoMapper.mapReverse(post)))
                            post
                        }

                        ApiResultCodes.ITEM_ALREADY_EXISTS.code -> {
                            getPost(id = "", url = post.url).getOrThrow()
                        }

                        else -> throw ApiException(networkResult.value.resultCode)
                    }
                }
            }

            is Failure -> {
                if (networkResult.value is IOException) {
                    addPostLocal(post = post)
                } else {
                    networkResult
                }
            }
        }
    }

    private suspend fun addPostLocal(post: Post): Result<Post> = resultFrom {
        val existingPost = postsDao.getPost(post.url)

        val newPost = PostDto(
            href = existingPost?.href ?: post.url,
            description = post.title,
            extended = post.description,
            hash = existingPost?.hash ?: post.id,
            time = existingPost?.time ?: post.dateAdded,
            shared = if (post.private == true) PinboardApiLiterals.NO else PinboardApiLiterals.YES,
            toread = if (post.readLater == true) PinboardApiLiterals.YES else PinboardApiLiterals.NO,
            tags = post.tags.orEmpty().joinToString(PinboardApiLiterals.TAG_SEPARATOR) { it.name },
            pendingSync = existingPost?.let { it.pendingSync ?: PendingSyncDto.UPDATE } ?: PendingSyncDto.ADD,
        )

        postsDao.savePosts(listOf(newPost))

        return@resultFrom postDtoMapper.map(newPost)
    }

    override suspend fun delete(post: Post): Result<Unit> {
        return if (connectivityInfoProvider.isConnected() && PendingSync.ADD != post.pendingSync) {
            deletePostRemote(post = post)
        } else {
            deletePostLocal(post = post)
        }
    }

    private suspend fun deletePostRemote(post: Post): Result<Unit> {
        val networkResult = resultFromNetwork {
            postsApi.delete(post.url)
        }

        return when (networkResult) {
            is Success -> {
                catching {
                    if (networkResult.value.resultCode == ApiResultCodes.DONE.code) {
                        postsDao.deletePost(post.url)
                    } else {
                        throw ApiException(networkResult.value.resultCode)
                    }
                }
            }

            is Failure -> deletePostLocal(post = post)
        }
    }

    private suspend fun deletePostLocal(post: Post): Result<Unit> = resultFrom {
        if (PendingSync.ADD == post.pendingSync) {
            postsDao.deletePost(post.url)
        } else {
            val existingPost = postsDao.getPost(post.url)
                ?: error("Can't delete post with url: ${post.url}. It doesn't exist locally.")

            postsDao.savePosts(listOf(existingPost.copy(pendingSync = PendingSyncDto.DELETE)))
        }
    }

    override fun getAllPosts(
        sortType: SortType,
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
                sortType,
                searchTerm,
                tags,
                untaggedOnly,
                postVisibility,
                readLaterOnly,
                countLimit,
                pageLimit,
                pageOffset,
                upToDate,
            )
        }

        if (!connectivityInfoProvider.isConnected()) {
            emit(localData(true))
            return@flow
        }

        emit(localData(false))

        val userLastUpdate = userRepository.lastUpdate.takeIf(String::isNotBlank)
        val apiLastUpdate = update().getOrDefault(dateFormatter.nowAsDataFormat())

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
        pagedRequestsJob?.cancel()

        val apiData = resultFromNetwork { postsApi.getAllPosts(offset = 0, limit = API_PAGE_SIZE) }
            .mapApiRequestFailure(endpoint = "posts/all")
            .mapCatching { posts ->
                postsDao.deleteAllSyncedPosts()
                savePosts(posts.let(postRemoteDtoMapper::mapList))

                userRepository.lastUpdate = apiLastUpdate

                getAdditionalPages(initialOffset = posts.size)
            }
            .let { localData(true) }

        emit(apiData)
    }

    @VisibleForTesting
    suspend fun getAdditionalPages(initialOffset: Int) = supervisorScope {
        if (API_PAGE_SIZE - initialOffset > MALFORMED_OBJECT_THRESHOLD) return@supervisorScope

        pagedRequestsJob = launch {
            runCatching {
                var currentOffset = initialOffset

                while (currentOffset != 0) {
                    val additionalPosts = postsApi.getAllPosts(
                        offset = currentOffset,
                        limit = API_PAGE_SIZE,
                    )

                    savePosts(postRemoteDtoMapper.mapList(additionalPosts))

                    if (API_PAGE_SIZE - additionalPosts.size < MALFORMED_OBJECT_THRESHOLD) {
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
        if (posts.isEmpty()) return

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
    ): Int {
        val isFtsCompatible = isFtsCompatible(searchTerm) &&
            (tags.isNullOrEmpty() || tags.all { isFtsCompatible(it.name) })

        return if (isFtsCompatible) {
            postsDao.getPostCount(
                term = PostsDao.preFormatTerm(searchTerm),
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicPostsOnly = postVisibility is PostVisibility.Public,
                privatePostsOnly = postVisibility is PostVisibility.Private,
                readLaterOnly = readLaterOnly,
                limit = countLimit,
            )
        } else {
            postsDao.getPostCountNoFts(
                term = searchTerm.trim(),
                tag1 = tags.getTagName(index = 0, preFormat = false),
                tag2 = tags.getTagName(index = 1, preFormat = false),
                tag3 = tags.getTagName(index = 2, preFormat = false),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicPostsOnly = postVisibility is PostVisibility.Public,
                privatePostsOnly = postVisibility is PostVisibility.Private,
                readLaterOnly = readLaterOnly,
                limit = countLimit,
            )
        }
    }

    @VisibleForTesting
    suspend fun getLocalData(
        sortType: SortType,
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
            searchTerm = searchTerm,
            tags = tags,
            untaggedOnly = untaggedOnly,
            postVisibility = postVisibility,
            readLaterOnly = readLaterOnly,
            countLimit = countLimit,
        )
        val isFtsCompatible = isFtsCompatible(searchTerm) &&
            (tags.isNullOrEmpty() || tags.all { isFtsCompatible(it.name) })

        val localData: List<Post> = when {
            localDataSize > 0 && isFtsCompatible -> {
                postsDao.getAllPosts(
                    sortType = sortType.index,
                    term = PostsDao.preFormatTerm(searchTerm),
                    tag1 = tags.getTagName(index = 0),
                    tag2 = tags.getTagName(index = 1),
                    tag3 = tags.getTagName(index = 2),
                    untaggedOnly = untaggedOnly,
                    ignoreVisibility = postVisibility is PostVisibility.None,
                    publicPostsOnly = postVisibility is PostVisibility.Public,
                    privatePostsOnly = postVisibility is PostVisibility.Private,
                    readLaterOnly = readLaterOnly,
                    limit = pageLimit,
                    offset = pageOffset,
                ).let(postDtoMapper::mapList)
            }

            localDataSize > 0 -> {
                postsDao.getAllPostsNoFts(
                    sortType = sortType.index,
                    term = searchTerm.trim(),
                    tag1 = tags.getTagName(index = 0, preFormat = false),
                    tag2 = tags.getTagName(index = 1, preFormat = false),
                    tag3 = tags.getTagName(index = 2, preFormat = false),
                    untaggedOnly = untaggedOnly,
                    ignoreVisibility = postVisibility is PostVisibility.None,
                    publicPostsOnly = postVisibility is PostVisibility.Public,
                    privatePostsOnly = postVisibility is PostVisibility.Private,
                    readLaterOnly = readLaterOnly,
                    limit = pageLimit,
                    offset = pageOffset,
                ).let(postDtoMapper::mapList)
            }

            else -> emptyList()
        }

        PostListResult(
            posts = localData,
            totalCount = localDataSize,
            upToDate = upToDate,
            canPaginate = localData.size == pageLimit,
        )
    }

    private fun List<Tag>?.getTagName(index: Int, preFormat: Boolean = true): String {
        val name: String = this?.getOrNull(index)?.name.orEmpty()

        return if (preFormat && name.isNotEmpty()) PostsDao.preFormatTag(name) else name
    }

    override suspend fun getPost(id: String, url: String): Result<Post> = resultFromNetwork {
        val post = postsDao.getPost(url)
            ?: postsApi.getPost(url).posts.firstOrNull()?.let(postRemoteDtoMapper::map)

        post?.let(postDtoMapper::map) ?: throw InvalidRequestException()
    }

    override suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag>,
    ): Result<List<String>> = resultFrom {
        val tagNames = currentTags.map(Tag::name)

        if (tag.isNotEmpty()) {
            postsDao.searchExistingPostTag(PostsDao.preFormatTag(tag))
                .flatMap { it.replaceHtmlChars().split(" ") }
                .filter { it.startsWith(tag, ignoreCase = true) && it !in tagNames }
                .distinct()
                .sorted()
        } else {
            postsDao.getAllPostTags()
                .asSequence()
                .flatMap { it.replaceHtmlChars().split(" ") }
                .groupBy { it }
                .map { (tag, postList) -> Tag(tag, postList.size) }
                .sortedByDescending { it.posts }
                .asSequence()
                .map { it.name }
                .filter { it !in tagNames }
                .take(20)
                .toList()
        }
    }

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = resultFrom {
        postsDao.getPendingSyncPosts().let(postDtoMapper::mapList)
    }

    override suspend fun clearCache(): Result<Unit> = resultFrom {
        postsDao.deleteAllPosts()
    }
}
