package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.platform.ResourceProvider
import com.fibelatti.core.functional.onFailure
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.core.extension.isServerException
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.user.domain.Login
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: Login,
    private val appStateRepository: AppStateRepository,
    private val resourceProvider: ResourceProvider,
) : BaseViewModel() {

    private val _screenState = MutableStateFlow(ScreenState())
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    fun login(
        apiToken: String,
        instanceUrl: String,
    ) {
        launch {
            _screenState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    apiTokenError = null,
                    instanceUrlError = null,
                )
            }

            val params = Login.Params(
                authToken = apiToken,
                instanceUrl = instanceUrl,
            )

            loginUseCase(params)
                .onFailure { error ->
                    _screenState.update { currentState ->
                        currentState.copy(isLoading = false)
                    }

                    val loginFailedCodes = listOf(
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                    )

                    when {
                        error is HttpException && error.code() in loginFailedCodes -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    apiTokenError = resourceProvider.getString(R.string.auth_token_error),
                                )
                            }
                        }

                        error.isServerException() -> {
                            _screenState.update { currentState ->
                                currentState.copy(
                                    apiTokenError = resourceProvider.getString(R.string.server_timeout_error),
                                )
                            }
                        }

                        else -> handleError(error)
                    }
                }
        }
    }

    fun logout() {
        launch {
            appStateRepository.runAction(UserLoggedOut)
        }
    }

    data class ScreenState(
        val isLoading: Boolean = false,
        val apiTokenError: String? = null,
        val instanceUrlError: String? = null,
    )
}
