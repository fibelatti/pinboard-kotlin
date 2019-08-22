package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class Login @Inject constructor(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository
) : UseCaseWithParams<Unit, String>() {

    override suspend fun run(params: String): Result<Unit> {
        userRepository.loginAttempt(params)

        return postsRepository.update()
            .map { postsRepository.clearCache() }
            .onSuccess { userRepository.loggedIn() }
    }
}
