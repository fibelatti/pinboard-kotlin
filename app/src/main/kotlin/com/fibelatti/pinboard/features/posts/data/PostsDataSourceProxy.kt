package com.fibelatti.pinboard.features.posts.data

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.linkding.data.PostsDataSourceLinkdingApi
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@Singleton
internal class PostsDataSourceProxy @Inject constructor(
    private val postsDataSourcePinboardApi: Provider<PostsDataSourcePinboardApi>,
    private val postsDataSourceLinkdingApi: Provider<PostsDataSourceLinkdingApi>,
    private val postsDataSourceNoApi: Provider<PostsDataSourceNoApi>,
    private val appModeProvider: AppModeProvider,
) : PostsRepository {

    private var currentAppMode: AppMode? = null
    private var currentRepository: PostsRepository? = null

    private val repository: PostsRepository
        get() = runBlocking {
            val appMode = appModeProvider.appMode.first { AppMode.UNSET != it }

            Timber.d("Getting repository (appMode=$appMode)")

            currentRepository?.takeIf { currentAppMode == appMode }
                ?: when (appMode) {
                    AppMode.NO_API -> postsDataSourceNoApi.get()
                    AppMode.PINBOARD -> postsDataSourcePinboardApi.get()
                    AppMode.LINKDING -> postsDataSourceLinkdingApi.get()
                    AppMode.UNSET -> throw IllegalStateException()
                }.also {
                    Timber.d("Setting repository (appMode=$appMode)")
                    currentAppMode = appMode
                    currentRepository = it
                }
        }

    override suspend fun update(): Result<String> = repository.update()

    override suspend fun add(post: Post): Result<Post> = repository.add(post = post)

    override suspend fun delete(post: Post): Result<Unit> = repository.delete(post = post)

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
