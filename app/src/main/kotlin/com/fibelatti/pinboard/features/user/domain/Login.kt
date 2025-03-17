package com.fibelatti.pinboard.features.user.domain

import com.fibelatti.core.functional.Result
import com.fibelatti.core.functional.UseCaseWithParams
import com.fibelatti.core.functional.map
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.AppModeProvider
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoginFailed
import com.fibelatti.pinboard.features.posts.domain.PostsRepository
import javax.inject.Inject
import timber.log.Timber

class Login @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val postsRepository: PostsRepository,
    private val appModeProvider: AppModeProvider,
) : UseCaseWithParams<Login.Params, Result<Unit>> {

    override suspend operator fun invoke(params: Params): Result<Unit> {
        Timber.d("Logging in (params=$params)")
        val appMode = when (params) {
            is PinboardParams -> AppMode.PINBOARD
            is LinkdingParams -> AppMode.LINKDING
        }
        when (params) {
            is PinboardParams -> {
                userRepository.setAuthToken(appMode = appMode, authToken = params.authToken.trim())
                appModeProvider.setSelection(appMode = appMode)
            }

            is LinkdingParams -> {
                userRepository.linkdingInstanceUrl = params.instanceUrl.trim()
                userRepository.setAuthToken(appMode = appMode, authToken = params.authToken.trim())
                appModeProvider.setSelection(appMode = appMode)
            }
        }

        return postsRepository.update()
            .map { postsRepository.clearCache() }
            .onSuccess { appStateRepository.runAction(UserLoggedIn(appMode = appMode)) }
            .onFailure { appStateRepository.runAction(UserLoginFailed(appMode = appMode)) }
    }

    sealed class Params {

        abstract val authToken: String
    }

    data class PinboardParams(override val authToken: String) : Params()

    data class LinkdingParams(override val authToken: String, val instanceUrl: String) : Params()
}
