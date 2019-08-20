package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.DarkTheme
import com.fibelatti.pinboard.core.android.LightTheme
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

    override suspend fun loginAttempt(authToken: String) {
        userSharedPreferences.setAuthToken(authToken)
        loginState.postValue(LoginState.Authorizing)
    }

    override suspend fun loggedIn() {
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

    override suspend fun getLastUpdate(): String = userSharedPreferences.getLastUpdate()

    override suspend fun setLastUpdate(value: String) {
        userSharedPreferences.setLastUpdate(value)
    }

    override suspend fun getAppearance(): Appearance {
        return if (userSharedPreferences.getAppearance() == LightTheme.value) {
            LightTheme
        } else {
            DarkTheme
        }
    }

    override suspend fun setAppearance(appearance: Appearance) {
        userSharedPreferences.setAppearance(appearance.value)
    }

    override suspend fun getAutoFillDescription(): Boolean =
        userSharedPreferences.getAutoFillDescription()

    override suspend fun setAutoFillDescription(value: Boolean) {
        userSharedPreferences.setAutoFillDescription(value)
    }

    override suspend fun getShowDescriptionInLists(): Boolean =
        userSharedPreferences.getShowDescriptionInLists()

    override suspend fun setShowDescriptionInLists(value: Boolean) {
        userSharedPreferences.setShowDescriptionInLists(value)
    }

    override suspend fun getShowDescriptionInDetails(): Boolean =
        userSharedPreferences.getShowDescriptionInDetails()

    override suspend fun setShowDescriptionInDetails(value: Boolean) {
        userSharedPreferences.setShowDescriptionInDetails(value)
    }

    override suspend fun getDefaultPrivate(): Boolean? = userSharedPreferences.getDefaultPrivate()

    override suspend fun setDefaultPrivate(value: Boolean) {
        userSharedPreferences.setDefaultPrivate(value)
    }

    override suspend fun getDefaultReadLater(): Boolean? = userSharedPreferences.getDefaultReadLater()

    override suspend fun setDefaultReadLater(value: Boolean) {
        userSharedPreferences.setDefaultReadLater(value)
    }

    override suspend fun getEditAfterSharing(): Boolean = userSharedPreferences.getEditAfterSharing()

    override suspend fun setEditAfterSharing(value: Boolean) {
        userSharedPreferences.setEditAfterSharing(value)
    }
}
