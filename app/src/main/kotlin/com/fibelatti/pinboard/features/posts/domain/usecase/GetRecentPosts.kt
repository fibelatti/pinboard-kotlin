package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.pinboard.core.AppConfig.DEFAULT_RECENT_QUANTITY
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.PostListResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentPosts @Inject constructor(
    private val postsRepository: PostsRepository
) {

    suspend operator fun invoke(params: GetPostParams): Flow<Result<PostListResult>> =
        postsRepository.getAllPosts(
            newestFirst = true,
            searchTerm = params.searchTerm,
            tags = (params.tagParams as? GetPostParams.Tags.Tagged)?.tags,
            untaggedOnly = false,
            publicPostsOnly = false,
            privatePostsOnly = false,
            readLaterOnly = false,
            countLimit = DEFAULT_RECENT_QUANTITY,
            pageLimit = DEFAULT_RECENT_QUANTITY,
            pageOffset = 0
        )
}
