package com.fibelatti.pinboard.features.user.data

import com.fibelatti.core.extension.orFalse
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.di.MainVariant
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
    @MainVariant private val mainVariant: Boolean,
) : UserRepository {

    private val _currentPreferences = MutableStateFlow(getPreferences())
    override val currentPreferences: Flow<UserPreferences> = _currentPreferences.asStateFlow()

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
            updateCurrentPreferences()
        }

    override var appearance: Appearance
        get() = when (userSharedPreferences.appearance) {
            Appearance.LightTheme.value -> Appearance.LightTheme
            Appearance.DarkTheme.value -> Appearance.DarkTheme
            else -> Appearance.SystemDefault
        }
        set(value) {
            userSharedPreferences.appearance = value.value
            updateCurrentPreferences()
        }

    override var applyDynamicColors: Boolean
        get() = userSharedPreferences.applyDynamicColors
        set(value) {
            userSharedPreferences.applyDynamicColors = value
            updateCurrentPreferences()
        }

    override var preferredDateFormat: PreferredDateFormat
        get() = when (userSharedPreferences.preferredDateFormat) {
            PreferredDateFormat.MonthDayYearWithTime.value -> PreferredDateFormat.MonthDayYearWithTime
            PreferredDateFormat.ShortYearMonthDayWithTime.value -> PreferredDateFormat.ShortYearMonthDayWithTime
            PreferredDateFormat.YearMonthDayWithTime.value -> PreferredDateFormat.YearMonthDayWithTime
            else -> PreferredDateFormat.DayMonthYearWithTime
        }
        set(value) {
            userSharedPreferences.preferredDateFormat = value.value
            updateCurrentPreferences()
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
            updateCurrentPreferences()
        }

    override var markAsReadOnOpen: Boolean
        get() = userSharedPreferences.markAsReadOnOpen
        set(value) {
            userSharedPreferences.markAsReadOnOpen = value
            updateCurrentPreferences()
        }

    override var autoFillDescription: Boolean
        get() = userSharedPreferences.autoFillDescription
        set(value) {
            userSharedPreferences.autoFillDescription = value
            updateCurrentPreferences()
        }

    override var showDescriptionInLists: Boolean
        get() = userSharedPreferences.showDescriptionInLists
        set(value) {
            userSharedPreferences.showDescriptionInLists = value
            updateCurrentPreferences()
        }

    override var defaultPrivate: Boolean?
        get() = userSharedPreferences.defaultPrivate
        set(value) {
            userSharedPreferences.defaultPrivate = value
            updateCurrentPreferences()
        }

    override var defaultReadLater: Boolean?
        get() = userSharedPreferences.defaultReadLater
        set(value) {
            userSharedPreferences.defaultReadLater = value
            updateCurrentPreferences()
        }

    override var editAfterSharing: EditAfterSharing
        get() = when (userSharedPreferences.editAfterSharing) {
            EditAfterSharing.BeforeSaving.value -> EditAfterSharing.BeforeSaving
            EditAfterSharing.AfterSaving.value -> EditAfterSharing.AfterSaving
            else -> EditAfterSharing.SkipEdit
        }
        set(value) {
            userSharedPreferences.editAfterSharing = value.value
            updateCurrentPreferences()
        }

    override var defaultTags: List<Tag>
        get() = userSharedPreferences.defaultTags.map(::Tag)
        set(value) {
            userSharedPreferences.defaultTags = value.map(Tag::name)
            updateCurrentPreferences()
        }

    private fun updateCurrentPreferences() {
        _currentPreferences.tryEmit(getPreferences())
    }

    private fun getPreferences(): UserPreferences = UserPreferences(
        periodicSync = periodicSync,
        appearance = appearance,
        applyDynamicColors = applyDynamicColors,
        preferredDateFormat = preferredDateFormat,
        preferredDetailsView = preferredDetailsView,
        autoFillDescription = autoFillDescription,
        showDescriptionInLists = showDescriptionInLists,
        defaultPrivate = defaultPrivate.orFalse(),
        defaultReadLater = defaultReadLater.orFalse(),
        editAfterSharing = editAfterSharing,
        defaultTags = defaultTags,
    )

    override fun hasAuthToken(): Boolean = userSharedPreferences.authToken.isNotEmpty() || !mainVariant

    override fun setAuthToken(authToken: String) {
        if (!mainVariant || authToken.isBlank()) return

        userSharedPreferences.authToken = authToken
    }

    override fun clearAuthToken() {
        if (!mainVariant) return

        userSharedPreferences.authToken = ""
        userSharedPreferences.lastUpdate = ""
    }
}
