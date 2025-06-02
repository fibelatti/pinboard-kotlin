package com.fibelatti.pinboard.features.linkding.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.resultFromNetwork
import com.fibelatti.pinboard.core.persistence.database.isFtsCompatible
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.data.model.PendingSyncDto
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PendingSync
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.io.IOException

internal class PostsDataSourceLinkdingApi @Inject constructor(
    private val linkdingApi: LinkdingApi,
    private val linkdingDao: BookmarksDao,
    private val bookmarkRemoteMapper: BookmarkRemoteMapper,
    private val bookmarkLocalMapper: BookmarkLocalMapper,
    private val dateFormatter: DateFormatter,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : PostsRepository {

    private var lastGetAllTimeMillis: Long = 0
    private var pagedRequestsJob: Job? = null

    /**
     * Update is only ever used outside the repository to verify the credentials, so try fetching a single bookmark.
     */
    override suspend fun update(): Result<String> = resultFromNetwork {
        linkdingApi.getBookmarks(limit = 1)
    }.mapCatching { dateFormatter.nowAsDataFormat() }

    override suspend fun add(post: Post): Result<Post> {
        val resolvedPost = post.copy(
            dateAdded = post.dateAdded.ifNullOrBlank { dateFormatter.nowAsDataFormat() },
            dateModified = dateFormatter.nowAsDataFormat(),
        )

        return if (connectivityInfoProvider.isConnected()) {
            addBookmarkRemote(post = resolvedPost)
        } else {
            addBookmarkLocal(post = resolvedPost)
        }
    }

    private suspend fun addBookmarkRemote(post: Post): Result<Post> {
        val resolvedId = post.id.ifBlank { null }?.toIntOrNull()
        val bookmarkRemote = BookmarkRemote(
            id = resolvedId,
            url = post.url,
            title = post.title,
            description = post.description,
            notes = post.notes,
            dateAdded = post.dateAdded,
            isArchived = post.isArchived,
            unread = post.readLater == true,
            shared = post.private != true,
            tagNames = post.tags?.map { it.name }.orEmpty(),
        )

        val networkResult = resultFromNetwork {
            if (resolvedId == null) {
                linkdingApi.createBookmark(bookmarkRemote = bookmarkRemote)
            } else {
                linkdingApi.updateBookmark(id = resolvedId.toString(), bookmarkRemote = bookmarkRemote)
            }
        }

        return when (networkResult) {
            is Success -> {
                catching {
                    val bookmark = bookmarkRemoteMapper.map(networkResult.value)

                    linkdingDao.deletePendingSyncBookmark(url = bookmark.url)
                    linkdingDao.saveBookmarks(listOf(bookmarkLocalMapper.mapReverse(bookmark)))

                    bookmark
                }
            }

            is Failure -> {
                if (networkResult.value is IOException) {
                    addBookmarkLocal(post = post)
                } else {
                    networkResult
                }
            }
        }
    }

    private suspend fun addBookmarkLocal(post: Post): Result<Post> = resultFrom {
        val existingPost = linkdingDao.getBookmark(id = post.id, url = post.url)

        val newPost = BookmarkLocal(
            id = post.id.ifEmpty { UUID.randomUUID().toString() },
            url = existingPost?.url ?: post.url,
            title = post.title,
            description = post.description,
            notes = post.notes,
            isArchived = post.isArchived,
            unread = post.readLater == true,
            shared = post.private != true,
            tagNames = post.tags?.joinToString(separator = " ") { it.name },
            dateAdded = post.dateAdded,
            dateModified = post.dateModified,
            pendingSync = existingPost?.let { it.pendingSync ?: PendingSyncDto.UPDATE } ?: PendingSyncDto.ADD,
        )

        linkdingDao.saveBookmarks(listOf(newPost))

        return@resultFrom bookmarkLocalMapper.map(newPost)
    }

    override suspend fun delete(post: Post): Result<Unit> {
        return if (connectivityInfoProvider.isConnected() && PendingSync.ADD != post.pendingSync) {
            deleteBookmarkRemote(post = post)
        } else {
            deleteBookmarkLocal(post = post)
        }
    }

    private suspend fun deleteBookmarkRemote(post: Post): Result<Unit> {
        val networkResult = resultFromNetwork {
            require(linkdingApi.deleteBookmark(id = post.id))
        }

        return when (networkResult) {
            is Success -> catching { linkdingDao.deleteBookmark(id = post.id) }
            is Failure -> deleteBookmarkLocal(post = post)
        }
    }

    private suspend fun deleteBookmarkLocal(post: Post): Result<Unit> = resultFrom {
        if (PendingSync.ADD == post.pendingSync) {
            linkdingDao.deleteBookmark(id = post.id)
        } else {
            val existingPost = linkdingDao.getBookmark(id = post.id, url = post.url)
                ?: error("Can't delete post with url: ${post.url}. It doesn't exist locally.")

            linkdingDao.saveBookmarks(listOf(existingPost.copy(pendingSync = PendingSyncDto.DELETE)))
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

        val shouldFetchRemote = connectivityInfoProvider.isConnected() &&
            (System.currentTimeMillis() - lastGetAllTimeMillis > 2.minutes.inWholeMilliseconds || forceRefresh)

        if (shouldFetchRemote) {
            emit(localData(false))
            getAllFromApi(localData)
        } else {
            emit(localData(true))
        }
    }

    private suspend fun FlowCollector<Result<PostListResult>>.getAllFromApi(
        localData: suspend (upToDate: Boolean) -> Result<PostListResult>,
    ) {
        pagedRequestsJob?.cancel()

        val apiData = resultFromNetwork { linkdingApi.getBookmarks(offset = 0, limit = AppConfig.API_PAGE_SIZE) }
            .mapCatching { paginatedResponse ->
                linkdingDao.deleteAllSyncedBookmarks()
                linkdingDao.saveBookmarks(
                    bookmarks = bookmarkRemoteMapper.mapList(paginatedResponse.results)
                        .let(bookmarkLocalMapper::mapListReverse),
                )

                lastGetAllTimeMillis = System.currentTimeMillis()

                getAdditionalPages(totalCount = paginatedResponse.count)
            }
            .let { localData(true) }

        emit(apiData)
    }

    @VisibleForTesting
    suspend fun getAdditionalPages(totalCount: Int) = supervisorScope {
        if (totalCount <= AppConfig.API_PAGE_SIZE) return@supervisorScope

        pagedRequestsJob = launch {
            runCatching {
                for (currentOffset in AppConfig.API_PAGE_SIZE until totalCount step AppConfig.API_PAGE_SIZE) {
                    val additionalPosts = linkdingApi.getBookmarks(
                        offset = currentOffset,
                        limit = AppConfig.API_PAGE_SIZE,
                    )

                    linkdingDao.saveBookmarks(
                        bookmarks = bookmarkRemoteMapper.mapList(additionalPosts.results)
                            .let(bookmarkLocalMapper::mapListReverse),
                    )
                }
            }
        }
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
            linkdingDao.getBookmarkCount(
                term = BookmarksDao.preFormatTerm(searchTerm),
                termNoFts = searchTerm,
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicBookmarksOnly = postVisibility is PostVisibility.Public,
                privateBookmarksOnly = postVisibility is PostVisibility.Private,
                readLaterOnly = readLaterOnly,
                limit = countLimit,
            )
        } else {
            linkdingDao.getBookmarkCountNoFts(
                term = searchTerm.trim(),
                tag1 = tags.getTagName(index = 0, preFormat = false),
                tag2 = tags.getTagName(index = 1, preFormat = false),
                tag3 = tags.getTagName(index = 2, preFormat = false),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicBookmarksOnly = postVisibility is PostVisibility.Public,
                privateBookmarksOnly = postVisibility is PostVisibility.Private,
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
                linkdingDao.getAllBookmarks(
                    sortType = sortType.index,
                    term = BookmarksDao.preFormatTerm(searchTerm),
                    termNoFts = searchTerm,
                    tag1 = tags.getTagName(index = 0),
                    tag2 = tags.getTagName(index = 1),
                    tag3 = tags.getTagName(index = 2),
                    untaggedOnly = untaggedOnly,
                    ignoreVisibility = postVisibility is PostVisibility.None,
                    publicBookmarksOnly = postVisibility is PostVisibility.Public,
                    privateBookmarksOnly = postVisibility is PostVisibility.Private,
                    readLaterOnly = readLaterOnly,
                    limit = pageLimit,
                    offset = pageOffset,
                ).let(bookmarkLocalMapper::mapList)
            }

            localDataSize > 0 -> {
                linkdingDao.getAllBookmarks(
                    sortType = sortType.index,
                    term = searchTerm.trim(),
                    tag1 = tags.getTagName(index = 0, preFormat = false),
                    tag2 = tags.getTagName(index = 1, preFormat = false),
                    tag3 = tags.getTagName(index = 2, preFormat = false),
                    untaggedOnly = untaggedOnly,
                    ignoreVisibility = postVisibility is PostVisibility.None,
                    publicBookmarksOnly = postVisibility is PostVisibility.Public,
                    privateBookmarksOnly = postVisibility is PostVisibility.Private,
                    readLaterOnly = readLaterOnly,
                    limit = pageLimit,
                    offset = pageOffset,
                ).let(bookmarkLocalMapper::mapList)
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

        return if (preFormat && name.isNotEmpty()) BookmarksDao.preFormatTag(name) else name
    }

    override suspend fun getPost(id: String, url: String): Result<Post> = resultFromNetwork {
        linkdingDao.getBookmark(id = id, url = url)?.let(bookmarkLocalMapper::map)
            ?: linkdingApi.getBookmark(id).let(bookmarkRemoteMapper::map)
    }

    override suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag>,
    ): Result<List<String>> = resultFrom {
        val isFtsCompatible = isFtsCompatible(tag)
        val tagNames = currentTags.map(Tag::name)

        if (tag.isNotEmpty()) {
            val tags = if (isFtsCompatible) {
                linkdingDao.searchExistingBookmarkTags(BookmarksDao.preFormatTag(tag))
            } else {
                linkdingDao.searchExistingBookmarkTagsNoFts(tag)
            }

            tags.flatMap { it.replaceHtmlChars().split(" ") }
                .filter { it.startsWith(tag, ignoreCase = true) && it !in tagNames }
                .distinct()
                .sorted()
        } else {
            linkdingDao.getAllBookmarkTags()
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
        linkdingDao.getPendingSyncBookmarks().let(bookmarkLocalMapper::mapList)
    }

    override suspend fun clearCache(): Result<Unit> = resultFrom {
        linkdingDao.deleteAllBookmarks()
        lastGetAllTimeMillis = 0
    }
}
