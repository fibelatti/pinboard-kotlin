package com.fibelatti.bookmarking.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.bookmarking.core.Config
import com.fibelatti.bookmarking.core.extension.replaceHtmlChars
import com.fibelatti.bookmarking.core.network.InvalidRequestException
import com.fibelatti.bookmarking.core.util.DateFormatter
import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.pinboard.data.PostDto
import com.fibelatti.bookmarking.pinboard.data.PostDtoMapper
import com.fibelatti.bookmarking.pinboard.data.PostsDao
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.resultFrom
import com.fibelatti.core.randomUUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class PostsDataSourceNoApi(
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val dateFormatter: DateFormatter,
) : PostsRepository {

    override suspend fun update(): Result<String> = Success(dateFormatter.nowAsTzFormat())

    override suspend fun add(post: Post): Result<Post> {
        val existingPost = resultFrom {
            postsDao.getPost(post.url)
        }.getOrNull()

        val newPost = PostDto(
            href = existingPost?.href ?: post.url,
            description = post.title,
            extended = post.description,
            hash = existingPost?.hash ?: post.id.ifEmpty { randomUUID() },
            time = existingPost?.time ?: post.time.ifEmpty { dateFormatter.nowAsTzFormat() },
            shared = if (post.private == true) Config.Pinboard.LITERAL_NO else Config.Pinboard.LITERAL_YES,
            toread = if (post.readLater == true) Config.Pinboard.LITERAL_YES else Config.Pinboard.LITERAL_NO,
            tags = post.tags?.joinToString(Config.Pinboard.TAG_SEPARATOR) { it.name }
                .orEmpty(),
        )

        return resultFrom {
            postsDao.savePosts(listOf(newPost))
            postDtoMapper.map(newPost)
        }
    }

    override suspend fun delete(id: String, url: String): Result<Unit> = resultFrom {
        postsDao.deletePost(url)
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
        val data = getLocalData(
            sortType = sortType,
            searchTerm = searchTerm,
            tags = tags,
            untaggedOnly = untaggedOnly,
            postVisibility = postVisibility,
            readLaterOnly = readLaterOnly,
            countLimit = countLimit,
            pageLimit = pageLimit,
            pageOffset = pageOffset,
        )

        emit(data)
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
            postsDao.getAllPosts(
                sortType = sortType.index,
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
                offset = pageOffset,
            ).let(postDtoMapper::mapList)
        } else {
            emptyList()
        }

        PostListResult(
            posts = localData,
            totalCount = localDataSize,
            upToDate = true,
            canPaginate = localData.size == countLimit,
        )
    }

    private fun List<Tag>?.getAndFormat(index: Int): String =
        this?.getOrNull(index)?.name?.let(PostsDao.Companion::preFormatTag).orEmpty()

    override suspend fun getPost(id: String, url: String): Result<Post> = resultFrom {
        postsDao.getPost(url)?.let(postDtoMapper::map) ?: throw InvalidRequestException()
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

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = Success(emptyList())

    override suspend fun clearCache(): Result<Unit> = resultFrom {
        postsDao.deleteAllPosts()
    }
}
