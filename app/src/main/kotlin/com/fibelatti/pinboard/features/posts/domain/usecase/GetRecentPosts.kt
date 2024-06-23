package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.bookmarking.core.Config.DEFAULT_RECENT_QUANTITY
import com.fibelatti.bookmarking.features.appstate.NewestFirst
import com.fibelatti.bookmarking.features.posts.domain.PostVisibility
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetRecentPosts(
    private val postsRepository: PostsRepository,
) {

    operator fun invoke(params: GetPostParams): Flow<Result<PostListResult>> = postsRepository.getAllPosts(
        sortType = NewestFirst,
        searchTerm = params.searchTerm,
        tags = (params.tags as? GetPostParams.Tags.Tagged)?.tags,
        untaggedOnly = false,
        postVisibility = PostVisibility.None,
        readLaterOnly = false,
        countLimit = DEFAULT_RECENT_QUANTITY,
        pageLimit = DEFAULT_RECENT_QUANTITY,
        pageOffset = 0,
        forceRefresh = params.forceRefresh,
    )
}
