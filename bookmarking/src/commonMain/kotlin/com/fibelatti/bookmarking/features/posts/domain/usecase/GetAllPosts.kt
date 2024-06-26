package com.fibelatti.bookmarking.features.posts.domain.usecase

import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.model.PostListResult
import com.fibelatti.core.functional.Result
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
public class GetAllPosts(
    private val postsRepository: PostsRepository,
) {

    public operator fun invoke(params: GetPostParams): Flow<Result<PostListResult>> = postsRepository.getAllPosts(
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
