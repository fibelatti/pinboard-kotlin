package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PostsDataSourceProxy @Inject constructor(
    private val postsDataSourcePinboardApi: PostsDataSourcePinboardApi,
    private val postsDataSourceNoApi: PostsDataSourceNoApi,
    private val appModeProvider: AppModeProvider,
) : PostsRepository {

    private val repository: PostsRepository
        get() = if (AppMode.PINBOARD == appModeProvider.appMode.value) {
            postsDataSourcePinboardApi
        } else {
            postsDataSourceNoApi
        }

    override suspend fun update(): Result<String> = repository.update()

    override suspend fun add(post: Post): Result<Post> = repository.add(
        post = post,
    )

    override suspend fun delete(
        url: String,
    ): Result<Unit> = repository.delete(
        url = url,
    )

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
    ): Flow<Result<PostListResult>> = repository.getAllPosts(
        sortType = sortType,
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
        currentTags: List<Tag>,
    ): Result<List<String>> = repository.searchExistingPostTag(
        tag = tag,
        currentTags = currentTags,
    )

    override suspend fun getPendingSyncPosts(): Result<List<Post>> = repository.getPendingSyncPosts()

    override suspend fun clearCache(): Result<Unit> = repository.clearCache()
}
