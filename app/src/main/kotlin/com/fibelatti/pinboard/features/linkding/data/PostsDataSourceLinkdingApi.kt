package com.fibelatti.pinboard.features.linkding.data

import androidx.annotation.VisibleForTesting
import androidx.room.RoomRawQuery
import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.core.functional.coRunCatching
import com.fibelatti.core.platform.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.AppConfig
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
import javax.inject.Inject
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.io.IOException
import timber.log.Timber

internal class PostsDataSourceLinkdingApi @Inject constructor(
    private val linkdingApi: LinkdingApi,
    private val linkdingDao: BookmarksDao,
    private val bookmarkRemoteMapper: BookmarkRemoteMapper,
    private val bookmarkLocalMapper: BookmarkLocalMapper,
    private val dateFormatter: DateFormatter,
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : PostsRepository {

    private var lastGetAll: Instant? = null
    private var lastGetArchived: Instant? = null

    @Volatile
    private var pagedRequestsJob: Job? = null

    /**
     * Update is only ever used outside the repository to verify the credentials, so try fetching a single bookmark.
     */
    override suspend fun update(): Result<String> = resultFromNetwork {
        linkdingApi.getBookmarks(limit = 1)
    }.coMapCatching { dateFormatter.nowAsDataFormat() }

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

        return networkResult.fold(
            onSuccess = {
                coRunCatching {
                    val bookmark = bookmarkRemoteMapper.map(it)

                    linkdingDao.deletePendingSyncBookmark(url = bookmark.url)
                    linkdingDao.saveBookmarks(listOf(bookmarkLocalMapper.mapReverse(bookmark)))

                    bookmark
                }
            },
            onFailure = {
                if (it is IOException) {
                    addBookmarkLocal(post = post)
                } else {
                    Result.failure(it)
                }
            },
        )
    }

    private suspend fun addBookmarkLocal(post: Post): Result<Post> = resultFrom {
        val existingPost = linkdingDao.getBookmark(id = post.id, url = post.url)

        val newPost = BookmarkLocal(
            id = post.id.ifEmpty { Uuid.random().toString() },
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

        return networkResult.fold(
            onSuccess = { coRunCatching { linkdingDao.deleteBookmark(id = post.id) } },
            onFailure = { deleteBookmarkLocal(post = post) },
        )
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

    override suspend fun archive(post: Post): Result<Post> = setArchived(post = post, archived = true)

    override suspend fun unarchive(post: Post): Result<Post> = setArchived(post = post, archived = false)

    private suspend fun setArchived(post: Post, archived: Boolean): Result<Post> {
        return if (connectivityInfoProvider.isConnected() && PendingSync.ADD != post.pendingSync) {
            setArchivedRemote(post = post, archived = archived)
        } else {
            setArchivedLocal(post = post, archived = archived)
        }
    }

    private suspend fun setArchivedRemote(post: Post, archived: Boolean): Result<Post> {
        val networkResult = resultFromNetwork {
            require(
                if (archived) {
                    linkdingApi.archiveBookmark(id = post.id)
                } else {
                    linkdingApi.unarchiveBookmark(id = post.id)
                },
            )
        }

        return networkResult.fold(
            onSuccess = { setArchivedLocal(post = post, archived = archived, pendingSync = null) },
            onFailure = {
                if (it is IOException) {
                    setArchivedLocal(post = post, archived = archived)
                } else {
                    Result.failure(it)
                }
            },
        )
    }

    private suspend fun setArchivedLocal(
        post: Post,
        archived: Boolean,
        pendingSync: PendingSyncDto? = if (archived) PendingSyncDto.ARCHIVE else PendingSyncDto.UNARCHIVE,
    ): Result<Post> = resultFrom {
        val existingPost = linkdingDao.getBookmark(id = post.id, url = post.url)
            ?: error("Can't archive post with url: ${post.url}. It doesn't exist locally.")

        // A bookmark still pending creation has no server id, so keep the ADD marker and let the
        // eventual create carry the archive state; otherwise apply the archive/unarchive marker.
        // Note: this checks the local row's marker, not the passed post's. setArchived() only routes
        // to the remote path when the passed post is not pending ADD, but the local row is the source
        // of truth here, so preserving its ADD marker is the safe choice even if the two disagree.
        val resolvedPendingSync = if (PendingSyncDto.ADD == existingPost.pendingSync) {
            PendingSyncDto.ADD
        } else {
            pendingSync
        }

        val updatedPost = existingPost.copy(isArchived = archived, pendingSync = resolvedPendingSync)
        linkdingDao.saveBookmarks(listOf(updatedPost))

        bookmarkLocalMapper.map(updatedPost)
    }

    override fun getAllPosts(
        sortType: SortType,
        searchTerm: String,
        tags: List<Tag>?,
        matchAll: Boolean,
        exactMatch: Boolean,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        archivedOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int,
        forceRefresh: Boolean,
    ): Flow<Result<PostListResult>> = flow {
        val localData: suspend (upToDate: Boolean) -> Result<PostListResult> = { upToDate: Boolean ->
            getLocalData(
                sortType = sortType,
                searchTerm = searchTerm,
                tags = tags,
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                archivedOnly = archivedOnly,
                countLimit = countLimit,
                pageLimit = pageLimit,
                pageOffset = pageOffset,
                upToDate = upToDate,
            )
        }

        val lastFetch: Instant? = if (archivedOnly) lastGetArchived else lastGetAll
        val shouldFetchRemote = connectivityInfoProvider.isConnected() &&
            (lastFetch == null || Clock.System.now() - lastFetch > 2.minutes || forceRefresh)

        if (shouldFetchRemote) {
            emit(localData(false))
            getAllFromApi(archivedOnly = archivedOnly, localData = localData)
        } else {
            emit(localData(true))
        }
    }

    private suspend fun FlowCollector<Result<PostListResult>>.getAllFromApi(
        archivedOnly: Boolean,
        localData: suspend (upToDate: Boolean) -> Result<PostListResult>,
    ) {
        pagedRequestsJob?.cancel()

        val apiData = resultFromNetwork { getBookmarksFromApi(archivedOnly = archivedOnly, offset = 0) }
            .coMapCatching { paginatedResponse ->
                linkdingDao.deleteSyncedBookmarks(archived = archivedOnly)
                linkdingDao.saveBookmarks(
                    bookmarks = bookmarkRemoteMapper.mapList(paginatedResponse.results)
                        .let(bookmarkLocalMapper::mapListReverse)
                        // Trust the endpoint over the payload: the archived list is archived, the rest is not.
                        .map { it.copy(isArchived = archivedOnly) },
                )

                if (archivedOnly) {
                    lastGetArchived = Clock.System.now()
                } else {
                    lastGetAll = Clock.System.now()
                }

                getAdditionalPages(archivedOnly = archivedOnly, totalCount = paginatedResponse.count)
            }
            .let { localData(true) }

        emit(apiData)
    }

    private suspend fun getBookmarksFromApi(
        archivedOnly: Boolean,
        offset: Int,
    ): PaginatedResponseRemote<BookmarkRemote> = if (archivedOnly) {
        linkdingApi.getArchivedBookmarks(offset = offset, limit = AppConfig.API_PAGE_SIZE)
    } else {
        linkdingApi.getBookmarks(offset = offset, limit = AppConfig.API_PAGE_SIZE)
    }

    @VisibleForTesting
    suspend fun getAdditionalPages(archivedOnly: Boolean, totalCount: Int) = supervisorScope {
        if (totalCount <= AppConfig.API_PAGE_SIZE) return@supervisorScope

        pagedRequestsJob = launch {
            try {
                for (currentOffset in AppConfig.API_PAGE_SIZE until totalCount step AppConfig.API_PAGE_SIZE) {
                    ensureActive()
                    val additionalPosts = getBookmarksFromApi(archivedOnly = archivedOnly, offset = currentOffset)

                    linkdingDao.saveBookmarks(
                        bookmarks = bookmarkRemoteMapper.mapList(additionalPosts.results)
                            .let(bookmarkLocalMapper::mapListReverse)
                            .map { it.copy(isArchived = archivedOnly) },
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Timber.e(e, "Failed to fetch additional pages")
            }
        }
    }

    override suspend fun getQueryResultSize(
        searchTerm: String,
        tags: List<Tag>?,
        matchAll: Boolean,
        exactMatch: Boolean,
    ): Int = coRunCatching {
        getLocalDataSize(
            searchTerm = searchTerm,
            tags = tags,
            matchAll = matchAll,
            exactMatch = exactMatch,
            untaggedOnly = false,
            postVisibility = PostVisibility.None,
            readLaterOnly = false,
            archivedOnly = false,
            countLimit = -1,
        )
    }.getOrDefault(0)

    @VisibleForTesting
    suspend fun getLocalDataSize(
        searchTerm: String,
        tags: List<Tag>?,
        matchAll: Boolean,
        exactMatch: Boolean,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        archivedOnly: Boolean,
        countLimit: Int,
    ): Int {
        val isFtsCompatible: Boolean = matchAll &&
            isFtsCompatible(searchTerm) &&
            (tags.isNullOrEmpty() || tags.all { isFtsCompatible(it.name) })

        val query: RoomRawQuery = if (isFtsCompatible) {
            BookmarksDao.bookmarksCountFtsQuery(
                term = searchTerm,
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                archivedOnly = archivedOnly,
                limit = countLimit,
            )
        } else {
            BookmarksDao.bookmarksCountNoFtsQuery(
                term = searchTerm,
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                archivedOnly = archivedOnly,
                limit = countLimit,
            )
        }

        return linkdingDao.getBookmarkCount(query = query)
    }

    @VisibleForTesting
    suspend fun getLocalData(
        sortType: SortType,
        searchTerm: String,
        tags: List<Tag>?,
        matchAll: Boolean,
        exactMatch: Boolean,
        untaggedOnly: Boolean,
        postVisibility: PostVisibility,
        readLaterOnly: Boolean,
        archivedOnly: Boolean,
        countLimit: Int,
        pageLimit: Int,
        pageOffset: Int,
        upToDate: Boolean,
    ): Result<PostListResult> = resultFrom {
        val localDataSize = getLocalDataSize(
            searchTerm = searchTerm,
            tags = tags,
            matchAll = matchAll,
            exactMatch = exactMatch,
            untaggedOnly = untaggedOnly,
            postVisibility = postVisibility,
            readLaterOnly = readLaterOnly,
            archivedOnly = archivedOnly,
            countLimit = countLimit,
        )
        val isFtsCompatible: Boolean = matchAll &&
            isFtsCompatible(searchTerm) &&
            (tags.isNullOrEmpty() || tags.all { isFtsCompatible(it.name) })
        val query: RoomRawQuery = if (isFtsCompatible) {
            BookmarksDao.allBookmarksFtsQuery(
                term = searchTerm,
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                archivedOnly = archivedOnly,
                sortType = sortType.index,
                offset = pageOffset,
                limit = pageLimit,
            )
        } else {
            BookmarksDao.allBookmarksNoFtsQuery(
                term = searchTerm,
                tag1 = tags.getTagName(index = 0),
                tag2 = tags.getTagName(index = 1),
                tag3 = tags.getTagName(index = 2),
                matchAll = matchAll,
                exactMatch = exactMatch,
                untaggedOnly = untaggedOnly,
                postVisibility = postVisibility,
                readLaterOnly = readLaterOnly,
                archivedOnly = archivedOnly,
                sortType = sortType.index,
                offset = pageOffset,
                limit = pageLimit,
            )
        }

        val localData: List<Post> = when {
            localDataSize > 0 -> linkdingDao.getAllBookmarks(query = query).let(bookmarkLocalMapper::mapList)
            else -> emptyList()
        }

        PostListResult(
            posts = localData,
            totalCount = localDataSize,
            upToDate = upToDate,
            canPaginate = localData.size == pageLimit,
        )
    }

    private fun List<Tag>?.getTagName(index: Int): String = this?.getOrNull(index)?.name.orEmpty()

    override suspend fun getPost(id: String, url: String): Result<Post> = resultFromNetwork {
        linkdingDao.getBookmark(id = id, url = url)?.let(bookmarkLocalMapper::map)
            ?: linkdingApi.getBookmark(id).let(bookmarkRemoteMapper::map)
    }

    override suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag>,
    ): Result<List<String>> = resultFrom {
        val isFtsCompatible: Boolean = isFtsCompatible(tag)
        val tagNames: List<String> = currentTags.map(Tag::name)

        if (tag.isNotEmpty()) {
            val query: RoomRawQuery = if (isFtsCompatible) {
                BookmarksDao.existingBookmarkTagFtsQuery(tag)
            } else {
                BookmarksDao.existingBookmarkTagNoFtsQuery(tag)
            }

            linkdingDao.searchExistingBookmarkTags(query = query)
                .flatMap { it.replaceHtmlChars().split(" ") }
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
        lastGetAll = null
    }
}
