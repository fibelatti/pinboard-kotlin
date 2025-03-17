package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.user.domain.Login
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ResponseException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val loginUseCase: Login,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel(scope, appStateRepository) {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    init {
        appState.map { it.content }
            .distinctUntilChangedBy { it::class }
            .filterIsInstance<LoginContent>()
            .onEach { loginContent ->
                _screenState.update {
                    ScreenState(
                        allowSwitching = loginContent.appMode == null,
                        useLinkding = loginContent.appMode == AppMode.LINKDING,
                    )
                }
            }
            .launchIn(scope)
    }

    fun useLinkding(value: Boolean) {
        _screenState.update { current -> current.copy(useLinkding = value) }
    }

    fun login(apiToken: String, instanceUrl: String) {
        if (screenState.value.useLinkding && instanceUrl.isBlank()) {
            _screenState.update { current ->
                current.copy(
                    isLoading = false,
                    apiTokenError = null,
                    instanceUrlError = resourceProvider.getString(R.string.auth_linkding_instance_url_error),
                )
            }
            return
        }

        if (apiToken.isBlank()) {
            _screenState.update { current ->
                current.copy(
                    isLoading = false,
                    apiTokenError = resourceProvider.getString(R.string.auth_token_empty),
                    instanceUrlError = null,
                )
            }
            return
        }

        scope.launch {
            _screenState.update { current ->
                current.copy(
                    isLoading = true,
                    apiTokenError = null,
                    instanceUrlError = null,
                )
            }

            val params = if (screenState.value.useLinkding) {
                Login.LinkdingParams(authToken = apiToken, instanceUrl = instanceUrl)
            } else {
                Login.PinboardParams(authToken = apiToken)
            }

            loginUseCase(params)
                .onFailure { error ->
                    when {
                        error is ResponseException && error.response.status.value in AppConfig.LOGIN_FAILED_CODES -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    apiTokenError = resourceProvider.getString(R.string.auth_token_error),
                                )
                            }
                        }

                        error.isServerException() -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    isLoading = false,
                                    apiTokenError = resourceProvider.getString(R.string.server_error),
                                )
                            }
                        }

                        else -> {
                            _screenState.update { current -> current.copy(isLoading = false) }
                            handleError(error)
                        }
                    }
                }
        }
    }

    data class ScreenState(
        val allowSwitching: Boolean = true,
        val useLinkding: Boolean = false,
        val isLoading: Boolean = false,
        val apiTokenError: String? = null,
        val instanceUrlError: String? = null,
    )
}
