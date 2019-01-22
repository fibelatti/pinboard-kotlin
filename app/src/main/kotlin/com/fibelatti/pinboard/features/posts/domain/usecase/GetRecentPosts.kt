package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.orTrue
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.Sorting
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class GetRecentPosts @Inject constructor(
    private val postsRepository: PostsRepository
) : UseCaseWithParams<List<Post>, GetRecentPosts.Params>() {

    override suspend fun run(params: GetRecentPosts.Params): Result<List<Post>> {
        val tags = params.tags?.take(AppConfig.API_FILTER_MAX_TAGS)

        return postsRepository.getRecentPosts(tags)
            .mapCatching { posts ->
                posts.filter { post -> tags?.intersect(post.tags)?.isNotEmpty().orTrue() }
                    .sort(params.sorting)
            }
    }

    private fun List<Post>.sort(sorting: Sorting) =
        when (sorting) {
            Sorting.NEWEST_FIRST -> sortedByDescending { it.time }
            Sorting.OLDEST_FIRST -> sortedBy { it.time }
        }

    data class Params(
        val tags: List<String>? = null,
        val sorting: Sorting = Sorting.NEWEST_FIRST
    )
}
