package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ResponseException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val userRepository: UserRepository,
    private val loginUseCase: Login,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel(scope, appStateRepository) {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    init {
        filteredContent<LoginContent>()
            .onEach { _screenState.update { ScreenState() } }
            .launchIn(scope)
    }

    fun login(apiToken: String, instanceUrl: String) {
        if (userRepository.useLinkding && instanceUrl.isBlank()) {
            _screenState.value = ScreenState(
                instanceUrlError = resourceProvider.getString(R.string.auth_linkding_instance_url_error),
            )
            return
        }

        if (apiToken.isBlank()) {
            _screenState.value = ScreenState(
                apiTokenError = resourceProvider.getString(R.string.auth_token_empty),
            )
            return
        }

        scope.launch {
            _screenState.value = ScreenState(isLoading = true)

            val params = Login.Params(
                authToken = apiToken,
                instanceUrl = instanceUrl,
            )

            loginUseCase(params)
                .onFailure { error ->
                    _screenState.value = ScreenState(isLoading = false)

                    when {
                        error is ResponseException && error.response.status.value in AppConfig.LOGIN_FAILED_CODES -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    apiTokenError = resourceProvider.getString(R.string.auth_token_error),
                                )
                            }
                        }

                        error.isServerException() -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    apiTokenError = resourceProvider.getString(R.string.server_error),
                                )
                            }
                        }

                        else -> handleError(error)
                    }
                }
        }
    }

    fun logout() {
        runAction(UserLoggedOut)
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        val apiTokenError: String? = null,
        val instanceUrlError: String? = null,
    )
}
