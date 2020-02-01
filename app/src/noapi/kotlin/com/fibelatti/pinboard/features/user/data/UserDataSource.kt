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

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : UserRepository {

    @VisibleForTesting
    val loginState = MutableLiveData<LoginState>().apply { value = LoginState.LoggedIn }

    override fun getLoginState(): LiveData<LoginState> = loginState

    override fun loginAttempt(authToken: String) {}

    override fun loggedIn() {}

    override fun logout() {}

    override fun forceLogout() {}

    override fun getLastUpdate(): String = ""

    override fun setLastUpdate(value: String) {}

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
        return when (userSharedPreferences.getPreferredDetailsView()) {
            PreferredDetailsView.ExternalBrowser.value -> PreferredDetailsView.ExternalBrowser
            PreferredDetailsView.Edit.value -> PreferredDetailsView.Edit
            else -> PreferredDetailsView.InAppBrowser
        }
    }

    override fun setPreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        userSharedPreferences.setPreferredDetailsView(preferredDetailsView.value)
    }

    override fun getAutoFillDescription(): Boolean =
        userSharedPreferences.getAutoFillDescription()

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

    override fun getDefaultPrivate(): Boolean? = null

    override fun setDefaultPrivate(value: Boolean) {}

    override fun getDefaultReadLater(): Boolean? = userSharedPreferences.getDefaultReadLater()

    override fun setDefaultReadLater(value: Boolean) {
        userSharedPreferences.setDefaultReadLater(value)
    }

    override fun getEditAfterSharing(): Boolean = userSharedPreferences.getEditAfterSharing()

    override fun setEditAfterSharing(value: Boolean) {
        userSharedPreferences.setEditAfterSharing(value)
    }
}
