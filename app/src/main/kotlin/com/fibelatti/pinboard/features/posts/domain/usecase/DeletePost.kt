package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Failure
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class DeletePost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl
) : UseCaseWithParams<Unit, String>() {

    override suspend fun run(params: String): Result<Unit> {
        return when (val urlResult = validateUrl(params)) {
            is Success -> postsRepository.delete(params)
            is Failure -> urlResult
        }
    }
}
