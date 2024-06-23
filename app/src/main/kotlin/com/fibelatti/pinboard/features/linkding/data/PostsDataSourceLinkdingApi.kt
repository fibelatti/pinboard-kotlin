package com.fibelatti.pinboard.features.linkding.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.bookmarking.core.Config
import com.fibelatti.bookmarking.core.extension.replaceHtmlChars
import com.fibelatti.bookmarking.core.network.resultFromNetwork
import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.posts.data.model.PendingSyncDto
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.linkding.data.BookmarkLocal
import com.fibelatti.bookmarking.linkding.data.BookmarkLocalMapper
import com.fibelatti.bookmarking.linkding.data.BookmarkRemote
import com.fibelatti.bookmarking.linkding.data.BookmarkRemoteMapper
import com.fibelatti.core.extension.ifNullOrBlank
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.core.functional.resultFrom
import com.fibelatti.core.randomUUID
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.minutes

@Factory
class PostsDataSourceLinkdingApi(
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
    }.mapCatching { dateFormatter.nowAsTzFormat() }

    override suspend fun add(post: Post): Result<Post> {
        val resolvedId = post.id.ifBlank { null }?.toIntOrNull()
        val resolvedPost = post.copy(
            time = post.time.ifNullOrBlank { dateFormatter.nowAsTzFormat() },
        )

        return if (connectivityInfoProvider.isConnected()) {
            addBookmarkRemote(id = resolvedId, post = resolvedPost)
        } else {
            addBookmarkLocal(id = resolvedId, post = resolvedPost)
        }
    }

    private suspend fun addBookmarkRemote(id: Int?, post: Post): Result<Post> {
        val bookmarkRemote = BookmarkRemote(
            id = id,
            url = post.url,
            title = post.title,
            description = post.description,
            notes = post.notes.orEmpty(),
            isArchived = post.isArchived ?: false,
            unread = post.readLater == true,
            shared = post.private != true,
            tagNames = post.tags?.map { it.name }.orEmpty(),
        )

        return resultFromNetwork {
            if (id == null) {
                linkdingApi.createBookmark(bookmarkRemote = bookmarkRemote)
            } else {
                linkdingApi.updateBookmark(id = id.toString(), bookmarkRemote = bookmarkRemote)
            }
        }.mapCatching(bookmarkRemoteMapper::map)
            .onSuccess {
                linkdingDao.deletePendingSyncBookmark(url = it.url)
                linkdingDao.saveBookmarks(listOf(bookmarkLocalMapper.mapReverse(it)))
            }
    }

    private suspend fun addBookmarkLocal(id: Int?, post: Post): Result<Post> = resultFrom {
        val existingPost = linkdingDao.getBookmark(id = id?.toString().orEmpty(), url = post.url)

        val newPost = BookmarkLocal(
            id = randomUUID(),
            url = existingPost?.url ?: post.url,
            title = post.title,
            description = post.description,
            notes = post.notes,
            isArchived = post.isArchived,
            unread = post.readLater == true,
            shared = post.private != true,
            tagNames = post.tags?.joinToString(separator = " ") { it.name },
            dateModified = dateFormatter.nowAsTzFormat(),
            pendingSync = existingPost?.let { it.pendingSync ?: PendingSyncDto.UPDATE } ?: PendingSyncDto.ADD,
        )

        linkdingDao.saveBookmarks(listOf(newPost))

        return@resultFrom bookmarkLocalMapper.map(newPost)
    }

    override suspend fun delete(id: String, url: String): Result<Unit> {
        return if (connectivityInfoProvider.isConnected()) {
            deleteBookmarkRemote(id = id)
        } else {
            deleteBookmarkLocal(id = id, url = url)
        }
    }

    private suspend fun deleteBookmarkRemote(id: String): Result<Unit> = resultFromNetwork {
        require(linkdingApi.deleteBookmark(id = id))
    }.onSuccess {
        linkdingDao.deleteBookmark(id = id)
    }

    private suspend fun deleteBookmarkLocal(id: String, url: String): Result<Unit> = resultFrom {
        val existingPost = linkdingDao.getBookmark(id = id, url = url)
            ?: throw IllegalStateException("Can't delete post with url: $url. It doesn't exist locally.")

        linkdingDao.saveBookmarks(listOf(existingPost.copy(pendingSync = PendingSyncDto.DELETE)))
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

        val apiData = resultFromNetwork { linkdingApi.getBookmarks(offset = 0, limit = Config.API_PAGE_SIZE) }
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
        if (totalCount <= Config.API_PAGE_SIZE) return@supervisorScope

        pagedRequestsJob = launch {
            runCatching {
                for (currentOffset in Config.API_PAGE_SIZE until totalCount step Config.API_PAGE_SIZE) {
                    val additionalPosts = linkdingApi.getBookmarks(
                        offset = currentOffset,
                        limit = Config.API_PAGE_SIZE,
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
    ): Int = linkdingDao.getBookmarkCount(
        term = BookmarksDao.preFormatTerm(searchTerm),
        tag1 = tags.getAndFormat(0),
        tag2 = tags.getAndFormat(1),
        tag3 = tags.getAndFormat(2),
        untaggedOnly = untaggedOnly,
        ignoreVisibility = postVisibility is PostVisibility.None,
        publicBookmarksOnly = postVisibility is PostVisibility.Public,
        privateBookmarksOnly = postVisibility is PostVisibility.Private,
        readLaterOnly = readLaterOnly,
        limit = countLimit,
    )

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
            searchTerm,
            tags,
            untaggedOnly,
            postVisibility,
            readLaterOnly,
            countLimit,
        )

        val localData = if (localDataSize > 0) {
            linkdingDao.getAllBookmarks(
                sortType = sortType.index,
                term = BookmarksDao.preFormatTerm(searchTerm),
                tag1 = tags.getAndFormat(0),
                tag2 = tags.getAndFormat(1),
                tag3 = tags.getAndFormat(2),
                untaggedOnly = untaggedOnly,
                ignoreVisibility = postVisibility is PostVisibility.None,
                publicBookmarksOnly = postVisibility is PostVisibility.Public,
                privateBookmarksOnly = postVisibility is PostVisibility.Private,
                readLaterOnly = readLaterOnly,
                limit = pageLimit,
                offset = pageOffset,
            ).let(bookmarkLocalMapper::mapList)
        } else {
            emptyList()
        }

        PostListResult(
            posts = localData,
            totalCount = localDataSize,
            upToDate = upToDate,
            canPaginate = localData.size == pageLimit,
        )
    }

    private fun List<Tag>?.getAndFormat(index: Int): String =
        this?.getOrNull(index)?.name?.let(BookmarksDao.Companion::preFormatTag).orEmpty()

    override suspend fun getPost(id: String, url: String): Result<Post> = resultFromNetwork {
        linkdingDao.getBookmark(id = id, url = url)?.let(bookmarkLocalMapper::map)
            ?: linkdingApi.getBookmark(id).let(bookmarkRemoteMapper::map)
    }

    override suspend fun searchExistingPostTag(
        tag: String,
        currentTags: List<Tag>,
    ): Result<List<String>> = resultFrom {
        val tagNames = currentTags.map(Tag::name)

        if (tag.isNotEmpty()) {
            linkdingDao.searchExistingBookmarkTags(BookmarksDao.preFormatTag(tag))
                .flatMap { it.replaceHtmlChars().split(" ") }
                .filter { it.startsWith(tag, ignoreCase = true) && it !in tagNames }
                .distinct()
                .sorted()
        } else {
            linkdingDao.getAllBookmarkTags()
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
    }
}
