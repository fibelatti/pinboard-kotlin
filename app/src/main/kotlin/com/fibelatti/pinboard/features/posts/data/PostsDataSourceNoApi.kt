package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrDefault
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.extension.replaceHtmlChars
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.network.InvalidRequestException
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class PostsDataSourceNoApi @Inject constructor(
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val dateFormatter: DateFormatter,
) : PostsRepository {

    override suspend fun update(): Result<String> = Success(dateFormatter.nowAsTzFormat())

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
        replace: Boolean,
    ): Result<Post> {
        val existingPost = resultFrom {
            postsDao.getPost(url)
        }.getOrNull()

        val newPost = PostDto(
            href = existingPost?.href ?: url,
            description = title,
            extended = description.orEmpty(),
            hash = existingPost?.hash ?: UUID.randomUUID().toString(),
            time = existingPost?.time ?: dateFormatter.nowAsTzFormat(),
            shared = if (private == true) AppConfig.PinboardApiLiterals.NO else AppConfig.PinboardApiLiterals.YES,
            toread = if (readLater == true) AppConfig.PinboardApiLiterals.YES else AppConfig.PinboardApiLiterals.NO,
            tags = tags?.joinToString(AppConfig.PinboardApiLiterals.TAG_SEPARATOR_RESPONSE) { it.name }
                .orEmpty(),
        )

        return resultFrom {
            postsDao.savePosts(listOf(newPost))
            postDtoMapper.map(newPost)
        }
    }

    override suspend fun delete(url: String): Result<Unit> = resultFrom {
        postsDao.deletePost(url)
    }

    override fun getAllPosts(
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
        val data = getLocalData(
            newestFirst = newestFirst,
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

        PostListResult(totalCount = localDataSize, posts = localData, upToDate = true)
    }

    private fun List<Tag>?.getAndFormat(index: Int): String =
        this?.getOrNull(index)?.name?.let(PostsDao.Companion::preFormatTag).orEmpty()

    override suspend fun getPost(url: String): Result<Post> = resultFrom {
        postsDao.getPost(url)?.let(postDtoMapper::map) ?: throw InvalidRequestException()
    }

    @Suppress("MagicNumber")
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
