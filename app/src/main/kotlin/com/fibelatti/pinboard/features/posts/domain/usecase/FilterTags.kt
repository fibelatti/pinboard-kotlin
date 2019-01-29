package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.extension.orTrue
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.catching
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject

class FilterTags @Inject constructor() : UseCaseWithParams<List<Post>, FilterTags.Params>() {

    override suspend fun run(params: Params): Result<List<Post>> = catching {
        params.posts.filter { post ->
            params.tags?.takeIf { it.isNotEmpty() }?.intersect(post.tags)?.isNotEmpty().orTrue()
        }
    }

    data class Params(val posts: List<Post>, val tags: List<String>?)
}
