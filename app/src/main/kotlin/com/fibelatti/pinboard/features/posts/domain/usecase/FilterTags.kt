package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class FilterTags @Inject constructor() : UseCaseWithParams<List<Post>, FilterTags.Params>() {

    override suspend fun run(params: Params): Result<List<Post>> = catching {
        val paramTags = params.tags?.takeIf { it.isNotEmpty() }

        if (paramTags == null) {
            params.posts
        } else {
            params.posts.filter { post -> post.tags.intersect(paramTags).isNotEmpty() }
        }
    }

    data class Params(val posts: List<Post>, val tags: List<String>?)
}
