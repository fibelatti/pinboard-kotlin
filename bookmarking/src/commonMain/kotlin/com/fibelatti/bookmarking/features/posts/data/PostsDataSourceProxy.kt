package com.fibelatti.bookmarking.features.posts.data

import com.fibelatti.bookmarking.core.AppMode
import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.features.appstate.SortType
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.Post
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.linkding.data.PostsDataSourceLinkdingApi
import com.fibelatti.bookmarking.pinboard.data.PostsDataSourcePinboardApi
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
internal class PostsDataSourceProxy(
    private val postsDataSourcePinboardApi: PostsDataSourcePinboardApi,
    private val postsDataSourceLinkdingApi: PostsDataSourceLinkdingApi,
    private val postsDataSourceNoApi: PostsDataSourceNoApi,
    private val appModeProvider: AppModeProvider,
) : PostsRepository {

    private val repository: PostsRepository
        get() = when (appModeProvider.appMode.value) {
            AppMode.NO_API -> postsDataSourceNoApi
            AppMode.PINBOARD -> postsDataSourcePinboardApi
            AppMode.LINKDING -> postsDataSourceLinkdingApi
        }

    override suspend fun update(): Result<String> = repository.update()

    override suspend fun add(post: Post): Result<Post> = repository.add(
        post = post,
    )

    override suspend fun delete(
        id: String,
        url: String,
    ): Result<Unit> = repository.delete(
        id = id,
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
        id: String,
        url: String,
    ): Result<Post> = repository.getPost(
        id = id,
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
