package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.ObservableUseCaseWithParams
import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetAllPosts @Inject constructor(
    private val postsRepository: PostsRepository,
) : ObservableUseCaseWithParams<GetPostParams, Result<PostListResult>> {

    override operator fun invoke(params: GetPostParams): Flow<Result<PostListResult>> = postsRepository.getAllPosts(
        sortType = params.sorting,
        searchTerm = params.searchTerm,
        tags = (params.tags as? GetPostParams.Tags.Tagged)?.tags,
        matchAll = params.matchAll,
        exactMatch = params.exactMatch,
        untaggedOnly = params.tags is GetPostParams.Tags.Untagged,
        postVisibility = params.visibility,
        readLaterOnly = params.readLater,
        countLimit = -1,
        pageLimit = params.limit,
        pageOffset = params.offset,
        forceRefresh = params.forceRefresh,
    )
}
