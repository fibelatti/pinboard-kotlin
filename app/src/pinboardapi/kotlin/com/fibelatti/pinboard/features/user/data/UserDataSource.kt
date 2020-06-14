package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("TooManyFunctions")
@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : UserRepository {

    @VisibleForTesting
    val loginState = MutableLiveData<LoginState>().apply {
        value = if (userSharedPreferences.getAuthToken().isNotEmpty()) {
            LoginState.LoggedIn
        } else {
            LoginState.LoggedOut
        }
    }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override fun loginAttempt(authToken: String) {
        userSharedPreferences.setAuthToken(authToken)
        loginState.postValue(LoginState.Authorizing)
    }

    override fun loggedIn() {
        loginState.postValue(LoginState.LoggedIn)
    }

    override fun logout() {
        userSharedPreferences.setAuthToken("")
        userSharedPreferences.setLastUpdate("")

        loginState.postValue(LoginState.LoggedOut)
    }

    override fun forceLogout() {
        if (loginState.value == LoginState.LoggedIn) {
            userSharedPreferences.setAuthToken("")
            userSharedPreferences.setLastUpdate("")

            loginState.postValue(LoginState.Unauthorized)
        }
    }

    override fun getLastUpdate(): String = userSharedPreferences.getLastUpdate()

    override fun setLastUpdate(value: String) {
        userSharedPreferences.setLastUpdate(value)
    }

    override fun getAppearance(): Appearance {
        return when (userSharedPreferences.getAppearance()) {
            Appearance.LightTheme.value -> Appearance.LightTheme
            Appearance.DarkTheme.value -> Appearance.DarkTheme
            else -> Appearance.SystemDefault
        }
    }

    override fun setAppearance(appearance: Appearance) {
        userSharedPreferences.setAppearance(appearance.value)
    }

    override fun getPreferredDetailsView(): PreferredDetailsView {
        val externalBrowser = PreferredDetailsView.ExternalBrowser(getMarkAsReadOnOpen())
        return when (userSharedPreferences.getPreferredDetailsView()) {
            externalBrowser.value -> externalBrowser
            PreferredDetailsView.Edit.value -> PreferredDetailsView.Edit
            else -> PreferredDetailsView.InAppBrowser(getMarkAsReadOnOpen())
        }
    }

    override fun setPreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        userSharedPreferences.setPreferredDetailsView(preferredDetailsView.value)
    }

    override fun getMarkAsReadOnOpen(): Boolean = userSharedPreferences.getMarkAsReadOnOpen()

    override fun setMarkAsReadOnOpen(value: Boolean) {
        userSharedPreferences.setMarkAsReadOnOpen(value)
    }

    override fun getAutoFillDescription(): Boolean = userSharedPreferences.getAutoFillDescription()

    override fun setAutoFillDescription(value: Boolean) {
        userSharedPreferences.setAutoFillDescription(value)
    }

    override fun getShowDescriptionInLists(): Boolean =
        userSharedPreferences.getShowDescriptionInLists()

    override fun setShowDescriptionInLists(value: Boolean) {
        userSharedPreferences.setShowDescriptionInLists(value)
    }

    override fun getShowDescriptionInDetails(): Boolean =
        userSharedPreferences.getShowDescriptionInDetails()

    override fun setShowDescriptionInDetails(value: Boolean) {
        userSharedPreferences.setShowDescriptionInDetails(value)
    }

    override fun getDefaultPrivate(): Boolean? = userSharedPreferences.getDefaultPrivate()

    override fun setDefaultPrivate(value: Boolean) {
        userSharedPreferences.setDefaultPrivate(value)
    }

    override fun getDefaultReadLater(): Boolean? = userSharedPreferences.getDefaultReadLater()

    override fun setDefaultReadLater(value: Boolean) {
        userSharedPreferences.setDefaultReadLater(value)
    }

    override fun getEditAfterSharing(): Boolean = userSharedPreferences.getEditAfterSharing()

    override fun setEditAfterSharing(value: Boolean) {
        userSharedPreferences.setEditAfterSharing(value)
    }
}
