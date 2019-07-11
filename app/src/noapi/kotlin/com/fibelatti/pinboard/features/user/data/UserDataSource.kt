package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor() : UserRepository {

    @VisibleForTesting
    val loginState = MutableLiveData<LoginState>().apply { value = LoginState.LoggedIn }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override suspend fun loginAttempt(authToken: String) {}

    override suspend fun loggedIn() {}

    override suspend fun logout() {}

    override suspend fun forceLogout() {}

    override suspend fun getLastUpdate(): String = ""

    override suspend fun setLastUpdate(value: String) {}
}
