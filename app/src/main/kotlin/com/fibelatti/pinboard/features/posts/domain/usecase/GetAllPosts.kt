package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.pinboard.features.appstate.NewestFirst
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class GetAllPosts @Inject constructor(
    private val postsRepository: PostsRepository
) : UseCaseWithParams<Pair<Int, List<Post>>?, GetPostParams>() {

    override suspend fun run(params: GetPostParams): Result<Pair<Int, List<Post>>?> =
        postsRepository.getAllPosts(
            newestFirst = params.sorting == NewestFirst,
            searchTerm = params.searchTerm,
            tags = if (params.tagParams is GetPostParams.Tags.Tagged) params.tagParams.tags else null,
            untaggedOnly = params.tagParams is GetPostParams.Tags.Untagged,
            publicPostsOnly = params.visibilityParams is GetPostParams.Visibility.Public,
            privatePostsOnly = params.visibilityParams is GetPostParams.Visibility.Private,
            readLaterOnly = params.readLater,
            countLimit = -1,
            pageLimit = params.limit,
            pageOffset = params.offset
        )
}
