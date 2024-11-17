package com.fibelatti.pinboard.features.user.data

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
) : UserRepository {

    private val _currentPreferences = MutableStateFlow(getPreferences())
    override val currentPreferences: StateFlow<UserPreferences> = _currentPreferences.asStateFlow()

    private val _authToken = MutableStateFlow(userSharedPreferences.authToken)
    override val authToken: StateFlow<String> = _authToken.asStateFlow()

    override var useLinkding: Boolean
        get() = userSharedPreferences.useLinkding
        set(value) {
            userSharedPreferences.useLinkding = value
            updateCurrentPreferences()
        }

    override var linkdingInstanceUrl: String
        get() = userSharedPreferences.linkdingInstanceUrl
        set(value) {
            userSharedPreferences.linkdingInstanceUrl = if (useLinkding) value else ""
            updateCurrentPreferences()
        }

    override var lastUpdate: String
        get() = userSharedPreferences.lastUpdate
        set(value) {
            userSharedPreferences.lastUpdate = value
        }

    override var periodicSync: PeriodicSync
        get() = when (userSharedPreferences.periodicSync) {
            PeriodicSync.Off.hours -> PeriodicSync.Off
            PeriodicSync.Every6Hours.hours -> PeriodicSync.Every6Hours
            PeriodicSync.Every12Hours.hours -> PeriodicSync.Every12Hours
            PeriodicSync.Every24Hours.hours -> PeriodicSync.Every24Hours
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
            Appearance.SystemDefault.value -> Appearance.SystemDefault
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

    override var disableScreenshots: Boolean
        get() = userSharedPreferences.disableScreenshots
        set(value) {
            userSharedPreferences.disableScreenshots = value
            updateCurrentPreferences()
        }

    override var preferredDateFormat: PreferredDateFormat
        get() = when (userSharedPreferences.preferredDateFormat) {
            PreferredDateFormat.DayMonthYearWithTime.value -> PreferredDateFormat.DayMonthYearWithTime
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
            val inAppBrowser = PreferredDetailsView.InAppBrowser(markAsReadOnOpen)
            val externalBrowser = PreferredDetailsView.ExternalBrowser(markAsReadOnOpen)
            return when (userSharedPreferences.preferredDetailsView) {
                inAppBrowser.value -> inAppBrowser
                externalBrowser.value -> externalBrowser
                PreferredDetailsView.Edit.value -> PreferredDetailsView.Edit
                else -> PreferredDetailsView.InAppBrowser(markAsReadOnOpen)
            }
        }
        set(value) {
            userSharedPreferences.preferredDetailsView = value.value
            updateCurrentPreferences()
        }

    override var alwaysUseSidePanel: Boolean
        get() = userSharedPreferences.alwaysUseSidePanel
        set(value) {
            userSharedPreferences.alwaysUseSidePanel = value
            updateCurrentPreferences()
        }

    override var markAsReadOnOpen: Boolean
        get() = userSharedPreferences.markAsReadOnOpen
        set(value) {
            userSharedPreferences.markAsReadOnOpen = value
            updateCurrentPreferences()
        }

    override var followRedirects: Boolean
        get() = userSharedPreferences.followRedirects
        set(value) {
            userSharedPreferences.followRedirects = value
            updateCurrentPreferences()
        }

    override var autoFillDescription: Boolean
        get() = userSharedPreferences.autoFillDescription
        set(value) {
            userSharedPreferences.autoFillDescription = value
            updateCurrentPreferences()
        }

    override var useBlockquote: Boolean
        get() = userSharedPreferences.useBlockquote
        set(value) {
            userSharedPreferences.useBlockquote = value
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
            else -> EditAfterSharing.AfterSaving
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
        _currentPreferences.value = getPreferences()
    }

    private fun getPreferences(): UserPreferences = UserPreferences(
        useLinkding = useLinkding,
        linkdingInstanceUrl = linkdingInstanceUrl,
        periodicSync = periodicSync,
        appearance = appearance,
        applyDynamicColors = applyDynamicColors,
        disableScreenshots = disableScreenshots,
        preferredDateFormat = preferredDateFormat,
        preferredDetailsView = preferredDetailsView,
        alwaysUseSidePanel = alwaysUseSidePanel,
        followRedirects = followRedirects,
        autoFillDescription = autoFillDescription,
        useBlockquote = useBlockquote,
        showDescriptionInLists = showDescriptionInLists,
        defaultPrivate = defaultPrivate ?: false,
        defaultReadLater = defaultReadLater ?: false,
        editAfterSharing = editAfterSharing,
        defaultTags = defaultTags,
    )

    override fun getUsername(): String = userSharedPreferences.authToken.substringBefore(":")

    override fun hasAuthToken(): Boolean = userSharedPreferences.authToken.isNotEmpty()

    override fun setAuthToken(authToken: String) {
        if (authToken.isNotBlank()) {
            _authToken.value = authToken
            userSharedPreferences.authToken = authToken
        }
    }

    override fun clearAuthToken() {
        userSharedPreferences.authToken = ""
        userSharedPreferences.lastUpdate = ""
        _authToken.value = ""
    }
}
