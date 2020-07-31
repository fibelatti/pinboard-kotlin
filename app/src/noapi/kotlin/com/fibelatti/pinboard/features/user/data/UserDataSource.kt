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
    val loginState = MutableLiveData<LoginState>().apply { value = LoginState.LoggedIn }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override fun loginAttempt(authToken: String) {
        // Intentionally empty
    }

    override fun loggedIn() {
        // Intentionally empty
    }

    override fun logout() {
        // Intentionally empty
    }

    override fun forceLogout() {
        // Intentionally empty
    }

    override fun getLastUpdate(): String = ""

    override fun setLastUpdate(value: String) {
        // Intentionally empty
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

    override fun getDefaultPrivate(): Boolean? = null

    override fun setDefaultPrivate(value: Boolean) {
        // Intentionally empty
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
