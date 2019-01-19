package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class Login @Inject constructor(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    unauthorizedHandler: UnauthorizedHandler
) : UseCaseWithParams<String, Login.Params>(), UnauthorizedHandler by unauthorizedHandler {

    override suspend fun run(params: Params): Result<String> {
        userRepository.login(params.apiToken)
        return postsRepository.update()
            .onSuccess { userRepository.loggedIn() }
            .handleUnauthorized()
    }

    data class Params(val apiToken: String)
}
