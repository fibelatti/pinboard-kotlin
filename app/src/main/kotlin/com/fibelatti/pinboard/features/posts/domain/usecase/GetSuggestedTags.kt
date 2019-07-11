package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import javax.inject.Inject

class GetSuggestedTags @Inject constructor(
    private val postsRepository: PostsRepository
) : UseCaseWithParams<List<String>, GetSuggestedTags.Params>() {

    override suspend fun run(params: Params): Result<List<String>> =
        postsRepository.searchExistingPostTag(params.tag)
            .mapCatching { postTags ->
                val currentTags = params.currentTags.map(Tag::name)
                postTags.filterNot { tag -> tag in currentTags }
            }

    data class Params(val tag: String, val currentTags: List<Tag>)
}
