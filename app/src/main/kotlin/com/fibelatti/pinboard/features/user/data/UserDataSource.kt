package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : UserRepository {

    @VisibleForTesting
    val loginState = MutableLiveData<LoginState>().apply {
        value = if (userSharedPreferences.getAuthToken().isNotEmpty()) LoginState.LoggedIn else LoginState.LoggedOut
    }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override fun loginAttempt(authToken: String) {
        userSharedPreferences.setAuthToken(authToken)
        this.loginState.postValue(LoginState.Authorizing)
    }

    override fun loggedIn() {
        this.loginState.postValue(LoginState.LoggedIn)
    }

    override fun logout() {
        userSharedPreferences.setAuthToken("")
        userSharedPreferences.setLastUpdate("")
        this.loginState.postValue(LoginState.LoggedOut)
    }

    override fun forceLogout() {
        if (loginState.value == LoginState.LoggedIn) {
            userSharedPreferences.setAuthToken("")
            userSharedPreferences.setLastUpdate("")
            this.loginState.postValue(LoginState.Unauthorized)
        }
    }

    override fun getLastUpdate(): String = userSharedPreferences.getLastUpdate()

    override fun setLastUpdate(value: String) {
        userSharedPreferences.setLastUpdate(value)
    }
}
