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
import com.fibelatti.pinboard.core.persistence.database.isFtsCompatible
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PostsDataSourceNoApi @Inject constructor(
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val dateFormatter: DateFormatter,
) : PostsRepository {

    override suspend fun update(): Result<String> = Success(dateFormatter.nowAsDataFormat())

    override suspend fun add(post: Post): Result<Post> {
        val existingPost = resultFrom {
            postsDao.getPost(post.url)
        }.getOrNull()

        val newPost = PostDto(
            href = existingPost?.href ?: post.url,
            description = post.title,
            extended = post.description,
            hash = existingPost?.hash ?: post.id.ifEmpty { UUID.randomUUID().toString() },
            time = existingPost?.time ?: post.dateAdded.ifEmpty { dateFormatter.nowAsDataFormat() },
            shared = if (post.private == true) {
                AppConfig.PinboardApiLiterals.NO
            } else {
                AppConfig.PinboardApiLiterals.YES
            },
            toread = if (post.readLater == true) {
                AppConfig.PinboardApiLiterals.YES
            } else {
                AppConfig.PinboardApiLiterals.NO
            },
            tags = post.tags?.joinToString(AppConfig.PinboardApiLiterals.TAG_SEPARATOR) { it.name }.orEmpty(),
        )

        return resultFrom {
            postsDao.savePosts(listOf(newPost))
            postDtoMapper.map(newPost)
        }
    }

    override suspend fun delete(post: Post): Result<Unit> = resultFrom {
        postsDao.deletePost(url = post.url)
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
            upToDate = true,
            canPaginate = localData.size == countLimit,
        )
    }

    private fun List<Tag>?.getTagName(index: Int, preFormat: Boolean = true): String {
        val name: String = this?.getOrNull(index)?.name.orEmpty()

        return if (preFormat && name.isNotEmpty()) PostsDao.preFormatTag(name) else name
    }

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

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = Success(emptyList())

    override suspend fun clearCache(): Result<Unit> = resultFrom {
        postsDao.deleteAllPosts()
    }
}
