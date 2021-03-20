package com.fibelatti.pinboard.features.user.data

import androidx.annotation.VisibleForTesting
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("TooManyFunctions")
@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
) : UserRepository {

    @VisibleForTesting
    val loginState = MutableStateFlow(
        value = if (userSharedPreferences.getAuthToken().isNotEmpty()) {
            LoginState.LoggedIn
        } else {
            LoginState.LoggedOut
        }
    )

    override fun getLoginState(): Flow<LoginState> = loginState

    override fun loginAttempt(authToken: String) {
        userSharedPreferences.setAuthToken(authToken)
        loginState.value = LoginState.Authorizing
    }

    override fun loggedIn() {
        loginState.value = LoginState.LoggedIn
    }

    override fun logout() {
        userSharedPreferences.setAuthToken("")
        userSharedPreferences.setLastUpdate("")

        loginState.value = LoginState.LoggedOut
    }

    override fun forceLogout() {
        if (loginState.value == LoginState.LoggedIn) {
            userSharedPreferences.setAuthToken("")
            userSharedPreferences.setLastUpdate("")

            loginState.value = LoginState.Unauthorized
        }
    }

    override fun getLastUpdate(): String = userSharedPreferences.getLastUpdate()

    override fun setLastUpdate(value: String) {
        userSharedPreferences.setLastUpdate(value)
    }

    override fun getAppearance(): Appearance = when (userSharedPreferences.getAppearance()) {
        Appearance.LightTheme.value -> Appearance.LightTheme
        Appearance.DarkTheme.value -> Appearance.DarkTheme
        else -> Appearance.SystemDefault
    }

    override fun setAppearance(appearance: Appearance) {
        userSharedPreferences.setAppearance(appearance.value)
    }

    override var preferredDateFormat: PreferredDateFormat
        get() = when (userSharedPreferences.preferredDateFormat) {
            PreferredDateFormat.MonthDayYearWithTime.value -> PreferredDateFormat.MonthDayYearWithTime
            else -> PreferredDateFormat.DayMonthYearWithTime
        }
        set(value) {
            userSharedPreferences.preferredDateFormat = value.value
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

    override fun getDefaultPrivate(): Boolean? = userSharedPreferences.getDefaultPrivate()

    override fun setDefaultPrivate(value: Boolean) {
        userSharedPreferences.setDefaultPrivate(value)
    }

    override fun getDefaultReadLater(): Boolean? = userSharedPreferences.getDefaultReadLater()

    override fun setDefaultReadLater(value: Boolean) {
        userSharedPreferences.setDefaultReadLater(value)
    }

    override fun getEditAfterSharing(): EditAfterSharing = when (userSharedPreferences.getEditAfterSharing()) {
        EditAfterSharing.BeforeSaving.value -> EditAfterSharing.BeforeSaving
        EditAfterSharing.AfterSaving.value -> EditAfterSharing.AfterSaving
        else -> EditAfterSharing.SkipEdit
    }

    override fun setEditAfterSharing(editAfterSharing: EditAfterSharing) {
        userSharedPreferences.setEditAfterSharing(editAfterSharing.value)
    }

    override fun getDefaultTags(): List<Tag> = userSharedPreferences.getDefaultTags().map(::Tag)

    override fun setDefaultTags(tags: List<Tag>) {
        userSharedPreferences.setDefaultTags(tags.map(Tag::name))
    }
}
