package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.ObservableUseCaseWithParams
import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.PostVisibility
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetRecentPosts @Inject constructor(
    private val postsRepository: PostsRepository,
) : ObservableUseCaseWithParams<SortType, Result<PostListResult>> {

    override operator fun invoke(params: SortType): Flow<Result<PostListResult>> = postsRepository.getAllPosts(
        sortType = params,
        searchTerm = "",
        tags = null,
        matchAll = true,
        exactMatch = false,
        untaggedOnly = false,
        postVisibility = PostVisibility.None,
        readLaterOnly = false,
        countLimit = DEFAULT_RECENT_QUANTITY,
        pageLimit = DEFAULT_RECENT_QUANTITY,
        pageOffset = 0,
        forceRefresh = false,
    )
}
