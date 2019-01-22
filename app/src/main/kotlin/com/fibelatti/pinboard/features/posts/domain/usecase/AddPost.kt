package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.value
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class AddPost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl
) : UseCaseWithParams<Unit, AddPost.Params>() {

    override suspend fun run(params: Params): Result<Unit> {
        return when (val urlResult = validateUrl(params.url)) {
            is Success -> {
                postsRepository.add(
                    url = urlResult.value,
                    description = params.description,
                    extended = params.extended,
                    tags = params.tags
                )
            }
            is Failure -> urlResult
        }
    }

    data class Params(
        val url: String,
        val description: String,
        val extended: String? = null,
        val tags: List<String>? = null
    )
}
