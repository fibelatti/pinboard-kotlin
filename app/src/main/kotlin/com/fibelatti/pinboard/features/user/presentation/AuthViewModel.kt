package com.fibelatti.pinboard.features.user.presentation

import androidx.lifecycle.LiveData
import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.core.functional.onFailure
import com.fibelatti.core.provider.ResourceProvider
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.network.isUnauthorized
import com.fibelatti.pinboard.features.user.domain.Login
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val login: Login,
    private val userRepository: UserRepository,
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {

    val loginState: LiveData<LoginState> get() = userRepository.getLoginState()

    val apiTokenError: LiveEvent<String> get() = _apiTokenError
    private val _apiTokenError = MutableLiveEvent<String>()

    fun login(apiToken: String) {
        launch {
            login(Login.Params(apiToken))
                .onFailure {
                    if (it.isUnauthorized()) {
                        _apiTokenError.postEvent(resourceProvider.getString(R.string.auth_token_error))
                    } else {
                        handleError(it)
                    }
                }
        }
    }

    fun logout() {
        userRepository.logout()
    }
}
