package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class FilterPosts @Inject constructor() : UseCaseWithParams<List<Post>, FilterPosts.Params>() {

    override suspend fun run(params: Params): Result<List<Post>> = catching {
        params.posts.filter { post ->
            when {
                params.term.isNotBlank() && params.tags.isNotEmpty() -> {
                    post.containsTerm(params.term) and post.tags.containsAll(params.tags)
                }
                params.term.isNotBlank() -> post.containsTerm(params.term)
                params.tags.isNotEmpty() -> post.tags.containsAll(params.tags)
                else -> true
            }
        }
    }

    private fun Post.containsTerm(term: String): Boolean =
        this.url.contains(term) or title.contains(term) or description.contains(term)

    data class Params(
        val posts: List<Post>,
        val term: String,
        val tags: List<Tag>
    )
}
