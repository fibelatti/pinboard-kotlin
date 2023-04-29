package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.android.ResourceProvider
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

    val apiTokenError: StateFlow<String?> get() = _apiTokenError
    private val _apiTokenError = MutableStateFlow<String?>(null)

    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    fun login(apiToken: String) {
        launch {
            _apiTokenError.value = null
            _loading.value = true

            loginUseCase(apiToken)
                .onFailure { error ->
                    _loading.value = false

                    val loginFailedCodes = listOf(
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                    )

                    when {
                        error is HttpException && error.code() in loginFailedCodes -> {
                            _apiTokenError.value = resourceProvider.getString(R.string.auth_token_error)
                        }
                        error.isServerException() -> {
                            _apiTokenError.value = resourceProvider.getString(R.string.server_timeout_error)
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
}
