package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class GetRecentPosts @Inject constructor(
    private val postsRepository: PostsRepository,
    private val filterPosts: FilterPosts,
    private val sort: Sort
) : UseCaseWithParams<List<Post>, GetParams>() {

    override suspend fun run(params: GetParams): Result<List<Post>> {
        return postsRepository.getRecentPosts(params.tags.takeIf { it.isNotEmpty() })
            .map { filterPosts(FilterPosts.Params(it, params.searchTerm, params.tags)) }
            .map { sort(Sort.Params(it, params.sorting)) }
    }
}
