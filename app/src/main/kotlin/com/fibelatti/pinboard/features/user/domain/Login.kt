package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.mapCatching
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class Login @Inject constructor(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository
) : UseCaseWithParams<Unit, Login.Params>() {

    override suspend fun run(params: Params): Result<Unit> {
        userRepository.loginAttempt(params.apiToken)

        return postsRepository.update()
            .onSuccess { userRepository.loggedIn() }
            .mapCatching { Unit }
    }

    class Params(val apiToken: String)
}
