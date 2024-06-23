package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import org.koin.core.annotation.Factory

@Factory
class Login(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
) : UseCaseWithParams<Unit, Login.Params>() {

    override suspend fun run(params: Params): Result<Unit> {
        userRepository.setAuthToken(params.authToken.trim())
        userRepository.linkdingInstanceUrl = params.instanceUrl.trim()

        return postsRepository.update()
            .map { postsRepository.clearCache() }
            .onSuccess { appStateRepository.runAction(UserLoggedIn) }
            .onFailure { appStateRepository.runAction(UserLoggedOut) }
    }

    data class Params(
        val authToken: String,
        val instanceUrl: String = "",
    )
}
