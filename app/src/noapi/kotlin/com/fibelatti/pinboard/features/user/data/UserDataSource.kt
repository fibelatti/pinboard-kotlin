package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.DarkTheme
import com.fibelatti.pinboard.core.android.LightTheme
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
    val loginState = MutableLiveData<LoginState>().apply { value = LoginState.LoggedIn }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override suspend fun loginAttempt(authToken: String) {}

    override suspend fun loggedIn() {}

    override suspend fun logout() {}

    override suspend fun forceLogout() {}

    override suspend fun getLastUpdate(): String = ""

    override suspend fun setLastUpdate(value: String) {}

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

    override suspend fun getDefaultPrivate(): Boolean? = null

    override suspend fun setDefaultPrivate(value: Boolean) {}

    override suspend fun getDefaultReadLater(): Boolean? = userSharedPreferences.getDefaultReadLater()

    override suspend fun setDefaultReadLater(value: Boolean) {
        userSharedPreferences.setDefaultReadLater(value)
    }
}
