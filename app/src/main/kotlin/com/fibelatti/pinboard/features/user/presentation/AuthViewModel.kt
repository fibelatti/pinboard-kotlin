package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel @Inject constructor(
    private val loginUseCase: Login,
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    val loginState: Flow<LoginState> get() = userRepository.getLoginState()

    val apiTokenError: Flow<String> get() = _apiTokenError.filterNotNull()
    private val _apiTokenError = MutableStateFlow<String?>(null)

    fun login(apiToken: String) {
        launch {
            loginUseCase(apiToken)
                .onFailure { error ->
                    val loginFailedCodes = listOf(
                        HttpURLConnection.HTTP_UNAUTHORIZED,
                        HttpURLConnection.HTTP_INTERNAL_ERROR,
                    )

                    if (error is HttpException && error.code() in loginFailedCodes) {
                        _apiTokenError.value = resourceProvider.getString(R.string.auth_token_error)
                    } else {
                        handleError(error)
                    }
                }
        }
    }

    fun logout() {
        launch {
            userRepository.logout()
        }
    }
}
