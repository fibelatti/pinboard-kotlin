package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.data.PostsDao
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
    private val postsDao: PostsDao
) : UserRepository {

    @VisibleForTesting
    val loginState = MutableLiveData<LoginState>().apply {
        value = if (userSharedPreferences.getAuthToken().isNotEmpty()) LoginState.LoggedIn else LoginState.LoggedOut
    }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override fun loginAttempt(authToken: String) {
        userSharedPreferences.setAuthToken(authToken)
        loginState.postValue(LoginState.Authorizing)
    }

    override fun loggedIn() {
        loginState.postValue(LoginState.LoggedIn)
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        userSharedPreferences.setAuthToken("")
        userSharedPreferences.setLastUpdate("")

        postsDao.deleteAllPosts()

        loginState.postValue(LoginState.LoggedOut)
    }

    override suspend fun forceLogout() = withContext(Dispatchers.IO) {
        if (loginState.value == LoginState.LoggedIn) {
            userSharedPreferences.setAuthToken("")
            userSharedPreferences.setLastUpdate("")

            postsDao.deleteAllPosts()

            loginState.postValue(LoginState.Unauthorized)
        }
    }

    override fun getLastUpdate(): String = userSharedPreferences.getLastUpdate()

    override fun setLastUpdate(value: String) {
        userSharedPreferences.setLastUpdate(value)
    }
}
