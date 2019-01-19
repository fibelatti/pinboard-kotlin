package com.fibelatti.pinboard.features.posts.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCase
import com.fibelatti.pinboard.features.user.domain.UnauthorizedHandler
import javax.inject.Inject

class Update @Inject constructor(
    private val postsRepository: PostsRepository,
    unauthorizedHandler: UnauthorizedHandler
) : UseCase<String>(), UnauthorizedHandler by unauthorizedHandler {

    override suspend fun run(): Result<String> =
        postsRepository.update().handleUnauthorized()
}
