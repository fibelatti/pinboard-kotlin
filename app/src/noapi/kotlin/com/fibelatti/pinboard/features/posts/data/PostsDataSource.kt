package com.fibelatti.pinboard.features.posts.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.core.extension.orFalse
import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
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
import java.lang.IllegalStateException
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
    ): Result<Unit> {
        val existingPost = resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.getPost(url)
            }
        }.getOrNull()

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

        return resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.savePosts(listOf(newPost))
            }
        }
    }

    override suspend fun delete(url: String): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.deletePost(url)
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
    ): Result<Pair<Int, List<Post>>?> =
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
                postsDao.getPost(url)
            }
        }.mapCatching(postDtoMapper::map)
    }

    override suspend fun searchExistingPostTag(tag: String): Result<List<String>> {
        return resultFrom {
            val concatenatedTags = withContext(Dispatchers.IO) {
                postsDao.searchExistingPostTag(PostsDao.preFormatTag(tag))
            }

            concatenatedTags.flatMap { it.split(" ") }
                .distinct()
                .filter { it.startsWith(tag) }
                .sorted()
        }
    }

    override suspend fun getSuggestedTagsForUrl(url: String): Result<SuggestedTags> =
        Failure(IllegalStateException("getSuggestedTagsForUrl should not be called in this flavor"))

    override suspend fun clearCache(): Result<Unit> {
        return resultFrom {
            withContext(Dispatchers.IO) {
                postsDao.deleteAllPosts()
            }
        }
    }
}
