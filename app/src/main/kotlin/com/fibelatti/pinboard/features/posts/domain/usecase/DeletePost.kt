package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeletePost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl,
) : UseCaseWithParams<Unit, String>() {

    override suspend fun run(params: String): Result<Unit> =
        validateUrl(params).map {
            withContext(NonCancellable) {
                postsRepository.delete(params)
            }
        }
}
