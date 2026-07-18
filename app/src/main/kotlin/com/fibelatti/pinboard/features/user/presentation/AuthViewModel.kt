package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.AppConfig
import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.LocalNetworkAccessProvider
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.LoginContent
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ResponseException
import java.net.ConnectException
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
    private val userRepository: UserRepository,
    private val localNetworkAccessProvider: LocalNetworkAccessProvider,
) : BaseViewModel(scope, appStateRepository) {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private var pendingLogin: PendingLogin? = null

    init {
        appState.map { it.content }
            .distinctUntilChangedBy { it::class }
            .filterIsInstance<LoginContent>()
            .onEach { loginContent ->
                _screenState.update {
                    ScreenState(
                        allowSwitching = loginContent.appMode == null,
                        useLinkding = loginContent.appMode == AppMode.LINKDING,
                        clientCertAlias = userRepository.linkdingClientCertAlias,
                    )
                }
            }
            .launchIn(scope)
    }

    fun useLinkding(value: Boolean) {
        _screenState.update { current -> current.copy(useLinkding = value) }
    }

    fun setClientCertAlias(alias: String?) {
        _screenState.update { current -> current.copy(clientCertAlias = alias) }
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

            if (screenState.value.useLinkding && localNetworkAccessProvider.isPermissionRequired(instanceUrl)) {
                pendingLogin = PendingLogin(apiToken = apiToken, instanceUrl = instanceUrl)
                _screenState.update { current ->
                    current.copy(isLoading = false, localNetworkPermissionRequired = true)
                }
                return@launch
            }

            performLogin(apiToken = apiToken, instanceUrl = instanceUrl)
        }
    }

    /**
     * Resumes a login that was held back by [localNetworkPermissionRequest] once the user has replied to the
     * permission request. [canRequestAgain] is false when the permission was denied for good, in which case
     * the system will no longer show its dialog and only the settings app can grant it. That case is handled
     * by the screen with a dialog that offers to open the settings app, so no inline error is set here.
     */
    fun localNetworkPermissionResult(granted: Boolean, canRequestAgain: Boolean) {
        val pending: PendingLogin = pendingLogin ?: return
        pendingLogin = null

        if (!granted) {
            _screenState.update { current ->
                current.copy(
                    isLoading = false,
                    instanceUrlError = if (canRequestAgain) {
                        resourceProvider.getString(R.string.auth_linkding_missing_local_network_permission)
                    } else {
                        null
                    },
                    localNetworkPermissionRequired = false,
                )
            }
            return
        }

        scope.launch {
            _screenState.update { current ->
                current.copy(isLoading = true, localNetworkPermissionRequired = false)
            }

            performLogin(apiToken = pending.apiToken, instanceUrl = pending.instanceUrl)
        }
    }

    private suspend fun performLogin(apiToken: String, instanceUrl: String) {
        val params: Login.Params = if (screenState.value.useLinkding) {
            Login.LinkdingParams(
                authToken = apiToken,
                instanceUrl = instanceUrl,
                clientCertAlias = screenState.value.clientCertAlias,
            )
        } else {
            Login.PinboardParams(
                authToken = apiToken,
            )
        }

        loginUseCase(params)
            .onFailure { error ->
                when {
                    error is ConnectException && screenState.value.useLinkding -> {
                        _screenState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                instanceUrlError = resourceProvider.getString(
                                    R.string.auth_linkding_unreachable_instance_url,
                                ),
                            )
                        }
                    }

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

    private data class PendingLogin(
        val apiToken: String,
        val instanceUrl: String,
    )

    data class ScreenState(
        val allowSwitching: Boolean = true,
        val useLinkding: Boolean = false,
        val clientCertAlias: String? = null,
        val isLoading: Boolean = false,
        val apiTokenError: String? = null,
        val instanceUrlError: String? = null,
        val localNetworkPermissionRequired: Boolean = false,
    )
}
