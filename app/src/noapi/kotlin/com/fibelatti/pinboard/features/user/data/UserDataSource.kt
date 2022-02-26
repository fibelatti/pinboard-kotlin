package com.fibelatti.pinboard.features.user.data

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.LoginState
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences
) : UserRepository {

    override val loginState: Flow<LoginState> = flowOf(LoginState.LoggedIn)

    override var lastUpdate: String
        get() = userSharedPreferences.lastUpdate
        set(value) {
            userSharedPreferences.lastUpdate = value
        }

    @Suppress("MagicNumber")
    override var periodicSync: PeriodicSync
        get() = when (userSharedPreferences.periodicSync) {
            6L -> PeriodicSync.Every6Hours
            12L -> PeriodicSync.Every12Hours
            24L -> PeriodicSync.Every24Hours
            else -> PeriodicSync.Off
        }
        set(value) {
            userSharedPreferences.periodicSync = value.hours
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
            PreferredDateFormat.YearMonthDayWithTime.value -> PreferredDateFormat.YearMonthDayWithTime
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
}
