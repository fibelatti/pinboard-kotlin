package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PostsDataSourceProxy @Inject constructor(
    private val postsDataSourcePinboardApi: PostsDataSourcePinboardApi,
    private val postsDataSourceNoApi: PostsDataSourceNoApi,
    private val userRepository: UserRepository,
    @MainVariant private val mainVariant: Boolean,
) : PostsRepository {

    private val repository: PostsRepository
        get() = if (mainVariant && !userRepository.appReviewMode) postsDataSourcePinboardApi else postsDataSourceNoApi

    override suspend fun update(): Result<String> = repository.update()

    override suspend fun add(
        url: String,
        title: String,
        description: String?,
        private: Boolean?,
        readLater: Boolean?,
        tags: List<Tag>?,
        replace: Boolean,
    ): Result<Post> = repository.add(
        url = url,
        title = title,
        description = description,
        private = private,
        readLater = readLater,
        tags = tags,
        replace = replace,
    )

    override suspend fun delete(
        url: String,
    ): Result<Unit> = repository.delete(
        url = url,
    )

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
    ): Flow<Result<PostListResult>> = repository.getAllPosts(
        newestFirst = newestFirst,
        searchTerm = searchTerm,
        tags = tags,
        untaggedOnly = untaggedOnly,
        postVisibility = postVisibility,
        readLaterOnly = readLaterOnly,
        countLimit = countLimit,
        pageLimit = pageLimit,
        pageOffset = pageOffset,
        forceRefresh = forceRefresh,
    )

    override suspend fun getQueryResultSize(
        searchTerm: String,
        tags: List<Tag>?,
    ): Int = repository.getQueryResultSize(
        searchTerm = searchTerm,
        tags = tags,
    )

    override suspend fun getPost(
        url: String,
    ): Result<Post> = repository.getPost(
        url = url,
    )

    override suspend fun searchExistingPostTag(
        tag: String,
    ): Result<List<String>> = repository.searchExistingPostTag(
        tag = tag,
    )

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = repository.getPendingSyncPosts()

    override suspend fun clearCache(): Result<Unit> = repository.clearCache()
}
