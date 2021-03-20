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

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
) : UserRepository {

    @VisibleForTesting
    @Suppress("VariableNaming")
    val _loginState = MutableStateFlow(
        value = if (userSharedPreferences.authToken.isNotEmpty()) {
            LoginState.LoggedIn
        } else {
            LoginState.LoggedOut
        }
    )

    override val loginState: Flow<LoginState>
        get() = _loginState

    override var lastUpdate: String
        get() = userSharedPreferences.lastUpdate
        set(value) {
            userSharedPreferences.lastUpdate = value
        }

    override var appearance: Appearance
        get() = when (userSharedPreferences.appearance) {
            Appearance.LightTheme.value -> Appearance.LightTheme
            Appearance.DarkTheme.value -> Appearance.DarkTheme
            else -> Appearance.SystemDefault
        }
        set(value) {
            userSharedPreferences.appearance = value.value
        }

    override var preferredDateFormat: PreferredDateFormat
        get() = when (userSharedPreferences.preferredDateFormat) {
            PreferredDateFormat.MonthDayYearWithTime.value -> PreferredDateFormat.MonthDayYearWithTime
            else -> PreferredDateFormat.DayMonthYearWithTime
        }
        set(value) {
            userSharedPreferences.preferredDateFormat = value.value
        }

    override var preferredDetailsView: PreferredDetailsView
        get() {
            val externalBrowser = PreferredDetailsView.ExternalBrowser(markAsReadOnOpen)
            return when (userSharedPreferences.preferredDetailsView) {
                externalBrowser.value -> externalBrowser
                PreferredDetailsView.Edit.value -> PreferredDetailsView.Edit
                else -> PreferredDetailsView.InAppBrowser(markAsReadOnOpen)
            }
        }
        set(value) {
            userSharedPreferences.preferredDetailsView = value.value
        }

    override var markAsReadOnOpen: Boolean
        get() = userSharedPreferences.markAsReadOnOpen
        set(value) {
            userSharedPreferences.markAsReadOnOpen = value
        }

    override var autoFillDescription: Boolean
        get() = userSharedPreferences.autoFillDescription
        set(value) {
            userSharedPreferences.autoFillDescription = value
        }

    override var showDescriptionInLists: Boolean
        get() = userSharedPreferences.showDescriptionInLists
        set(value) {
            userSharedPreferences.showDescriptionInLists = value
        }

    override var defaultPrivate: Boolean?
        get() = userSharedPreferences.defaultPrivate
        set(value) {
            userSharedPreferences.defaultPrivate = value
        }

    override var defaultReadLater: Boolean?
        get() = userSharedPreferences.defaultReadLater
        set(value) {
            userSharedPreferences.defaultReadLater = value
        }

    override var editAfterSharing: EditAfterSharing
        get() = when (userSharedPreferences.editAfterSharing) {
            EditAfterSharing.BeforeSaving.value -> EditAfterSharing.BeforeSaving
            EditAfterSharing.AfterSaving.value -> EditAfterSharing.AfterSaving
            else -> EditAfterSharing.SkipEdit
        }
        set(value) {
            userSharedPreferences.editAfterSharing = value.value
        }

    override var defaultTags: List<Tag>
        get() = userSharedPreferences.defaultTags.map(::Tag)
        set(value) {
            userSharedPreferences.defaultTags = value.map(Tag::name)
        }

    override fun loginAttempt(authToken: String) {
        userSharedPreferences.authToken = authToken
        _loginState.value = LoginState.Authorizing
    }

    override fun loggedIn() {
        _loginState.value = LoginState.LoggedIn
    }

    override fun logout() {
        userSharedPreferences.authToken = ""
        userSharedPreferences.lastUpdate = ""

        _loginState.value = LoginState.LoggedOut
    }

    override fun forceLogout() {
        if (_loginState.value == LoginState.LoggedIn) {
            userSharedPreferences.authToken = ""
            userSharedPreferences.lastUpdate = ""

            _loginState.value = LoginState.Unauthorized
        }
    }
}
