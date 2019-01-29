package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.SuggestedTags
import javax.inject.Inject

class GetSuggestedTagsForUrl @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl
) : UseCaseWithParams<SuggestedTags, String>() {

    override suspend fun run(params: String): Result<SuggestedTags> =
        validateUrl(params).map { postsRepository.getSuggestedTagsForUrl(params) }
}
