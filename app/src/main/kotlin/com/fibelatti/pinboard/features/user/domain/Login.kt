package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.Success
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject

class Login @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository
) : UseCaseWithParams<Unit, String>() {

    override suspend fun run(params: String): Result<Unit> {
        if (params == "app_review_mode") {
            userRepository.appReviewMode = true
            postsRepository.clearCache()
            appStateRepository.runAction(UserLoggedIn)

            return Success(Unit)
        }

        userRepository.setAuthToken(params.trim())

        return postsRepository.update()
            .map { postsRepository.clearCache() }
            .onSuccess { appStateRepository.runAction(UserLoggedIn) }
            .onFailure { appStateRepository.runAction(UserLoggedOut) }
    }
}
