package com.fibelatti.pinboard.features.posts.domain.usecase

import com.fibelatti.core.functional.ResultUseCaseWithParams
import com.fibelatti.core.functional.coMapCatching
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import com.fibelatti.pinboard.features.posts.domain.model.Post
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class DeletePost @Inject constructor(
    private val postsRepository: PostsRepository,
    private val validateUrl: ValidateUrl,
) : ResultUseCaseWithParams<Post, Unit> {

    override suspend operator fun invoke(params: Post): Result<Unit> =
        validateUrl(params.url).coMapCatching {
            withContext(NonCancellable) {
                postsRepository.delete(post = params).getOrThrow()
            }
        }
}
