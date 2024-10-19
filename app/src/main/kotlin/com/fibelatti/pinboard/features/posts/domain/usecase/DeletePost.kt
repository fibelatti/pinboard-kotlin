package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class DeletePost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl,
) : UseCaseWithParams<Post, Result<Unit>> {

    override suspend operator fun invoke(params: Post): Result<Unit> =
        validateUrl(params.url).map {
            withContext(NonCancellable) {
                postsRepository.delete(post = params)
            }
        }
}
