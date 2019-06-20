package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class FilterPosts @Inject constructor() : UseCaseWithParams<List<Post>, FilterPosts.Params>() {

    override suspend fun run(params: Params): Result<List<Post>> = catching {
        val paramTags = params.tags?.takeIf { it.isNotEmpty() }

        params.posts.filter { post ->
            when {
                params.term.isNotBlank() && paramTags != null -> {
                    post.containsTerm(params.term) and post.tags.containsAll(paramTags)
                }
                params.term.isNotBlank() -> {
                    post.containsTerm(params.term)
                }
                paramTags != null -> post.tags.containsAll(paramTags)
                else -> true
            }
        }
    }

    private fun Post.containsTerm(term: String): Boolean =
        this.url.contains(term) or description.contains(term) or extendedDescription.contains(term)

    data class Params(
        val posts: List<Post>,
        val term: String,
        val tags: List<String>?
    )
}
