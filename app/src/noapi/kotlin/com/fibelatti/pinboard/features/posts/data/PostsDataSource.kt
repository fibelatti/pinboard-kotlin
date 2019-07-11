package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.orFalse
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.catching
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.functional.resultFrom
import com.fibelatti.pinboard.core.util.DateFormatter
import com.fibelatti.pinboard.features.posts.data.model.PostDto
import com.fibelatti.pinboard.features.posts.data.model.PostDtoMapper
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class PostsDataSource @Inject constructor(
    private val postsDao: PostsDao,
    private val postDtoMapper: PostDtoMapper,
    private val dateFormatter: DateFormatter
) : PostsRepository {

    override suspend fun update(): Result<String> = Success(dateFormatter.nowAsTzFormat())

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val existingPost = catching { postsDao.getPost(url) }.getOrNull()

        val newPost = PostDto(
            href = existingPost?.href ?: url,
            description = title,
            extended = description.orEmpty(),
            hash = existingPost?.hash ?: UUID.randomUUID().toString(),
            time = existingPost?.time ?: dateFormatter.nowAsTzFormat(),
            shared = if (private.orFalse()) AppConfig.PinboardApiLiterals.NO else AppConfig.PinboardApiLiterals.YES,
            toread = if (readLater.orFalse()) AppConfig.PinboardApiLiterals.YES else AppConfig.PinboardApiLiterals.NO,
            tags = tags?.joinToString(AppConfig.PinboardApiLiterals.TAG_SEPARATOR_RESPONSE) { it.name }.orEmpty(),
            imageUrl = null
        )

        resultFrom { postsDao.savePosts(listOf(newPost)) }
    }

    override suspend fun delete(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        resultFrom { postsDao.deletePost(url) }
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
    ): Result<Pair<Int, List<Post>>?> = withContext(Dispatchers.IO) {
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

    @VisibleForTesting
    fun getLocalDataSize(
        searchTerm: String,
        tags: List<Tag>?,
        untaggedOnly: Boolean,
        publicPostsOnly: Boolean,
        privatePostsOnly: Boolean,
        readLaterOnly: Boolean,
        countLimit: Int
    ): Int {
        return postsDao.getPostCount(
            term = PostsDao.preFormatTerm(searchTerm),
            tag1 = tags?.getOrNull(0)?.name.orEmpty(),
            tag2 = tags?.getOrNull(1)?.name.orEmpty(),
            tag3 = tags?.getOrNull(2)?.name.orEmpty(),
            untaggedOnly = untaggedOnly,
            publicPostsOnly = publicPostsOnly,
            privatePostsOnly = privatePostsOnly,
            readLaterOnly = readLaterOnly,
            limit = countLimit
        )
    }

    @VisibleForTesting
    fun getLocalData(
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
        return catching {
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
                localDataSize to postsDao.getAllPosts(
                    newestFirst = newestFirst,
                    term = PostsDao.preFormatTerm(searchTerm),
                    tag1 = tags?.getOrNull(0)?.name.orEmpty(),
                    tag2 = tags?.getOrNull(1)?.name.orEmpty(),
                    tag3 = tags?.getOrNull(2)?.name.orEmpty(),
                    untaggedOnly = untaggedOnly,
                    publicPostsOnly = publicPostsOnly,
                    privatePostsOnly = privatePostsOnly,
                    readLaterOnly = readLaterOnly,
                    limit = pageLimit,
                    offset = pageOffset
                ).let(postDtoMapper::mapList)
            } else {
                null
            }
        }
    }

    override suspend fun getPost(url: String): Result<Post> = withContext(Dispatchers.IO) {
        resultFrom { postsDao.getPost(url) }.mapCatching(postDtoMapper::map)
    }

    override suspend fun searchExistingPostTag(tag: String): Result<List<String>> {
        return resultFrom {
            val concatenatedTags = withContext(Dispatchers.IO) {
                postsDao.searchExistingPostTag(PostsDao.preFormatTagForSearch(tag))
            }

            concatenatedTags.flatMap { it.split(" ") }
                .distinct()
                .filter { it.startsWith(tag) }
                .sorted()
        }
    }

    override suspend fun getSuggestedTagsForUrl(url: String): Result<SuggestedTags> {
        return Failure(Exception())
    }
}
