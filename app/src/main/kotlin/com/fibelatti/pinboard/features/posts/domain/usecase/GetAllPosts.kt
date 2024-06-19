package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetAllPosts(
    private val postsRepository: PostsRepository,
) {

    operator fun invoke(params: GetPostParams): Flow<Result<PostListResult>> = postsRepository.getAllPosts(
        sortType = params.sorting,
        searchTerm = params.searchTerm,
        tags = (params.tags as? GetPostParams.Tags.Tagged)?.tags,
        untaggedOnly = params.tags is GetPostParams.Tags.Untagged,
        postVisibility = params.visibility,
        readLaterOnly = params.readLater,
        countLimit = -1,
        pageLimit = params.limit,
        pageOffset = params.offset,
        forceRefresh = params.forceRefresh,
    )
}
