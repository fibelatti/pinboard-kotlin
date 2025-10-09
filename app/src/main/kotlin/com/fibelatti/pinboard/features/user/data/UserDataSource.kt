package com.fibelatti.pinboard.features.user.data

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.persistence.UserSharedPreferences
import com.fibelatti.pinboard.features.appstate.ByDateAddedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateAddedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedNewestFirst
import com.fibelatti.pinboard.features.appstate.ByDateModifiedOldestFirst
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabetical
import com.fibelatti.pinboard.features.appstate.ByTitleAlphabeticalReverse
import com.fibelatti.pinboard.features.appstate.SortType
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class UserDataSource @Inject constructor(
    private val userSharedPreferences: UserSharedPreferences,
) : UserRepository {

    private val _userCredentials = MutableStateFlow(getUserCredentials())
    override val userCredentials: StateFlow<UserCredentials> = _userCredentials.asStateFlow()

    private val _currentPreferences = MutableStateFlow(getPreferences())
    override val currentPreferences: StateFlow<UserPreferences> = _currentPreferences.asStateFlow()

    override var linkdingInstanceUrl: String?
        get() = userSharedPreferences.linkdingInstanceUrl
        set(value) {
            userSharedPreferences.linkdingInstanceUrl = value
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
        get() {
            val includeTime: Boolean = userSharedPreferences.dateIncludesTime
            val formats: List<PreferredDateFormat> = listOf(
                PreferredDateFormat.DayMonthYearWithTime(includeTime = includeTime),
                PreferredDateFormat.MonthDayYearWithTime(includeTime = includeTime),
                PreferredDateFormat.ShortYearMonthDayWithTime(includeTime = includeTime),
                PreferredDateFormat.YearMonthDayWithTime(includeTime = includeTime),
                PreferredDateFormat.NoDate,
            )

            return formats.firstOrNull { it.value == userSharedPreferences.preferredDateFormat }
                ?: formats.first()
        }
        set(value) {
            userSharedPreferences.preferredDateFormat = value.value
            userSharedPreferences.dateIncludesTime = value.includeTime
            updateCurrentPreferences()
        }

    override var preferredSortType: SortType
        get() = when (userSharedPreferences.preferredSortType) {
            ByDateAddedOldestFirst.value -> ByDateAddedOldestFirst
            ByDateModifiedNewestFirst.value -> ByDateModifiedNewestFirst
            ByDateModifiedOldestFirst.value -> ByDateModifiedOldestFirst
            ByTitleAlphabetical.value -> ByTitleAlphabetical
            ByTitleAlphabeticalReverse.value -> ByTitleAlphabeticalReverse
            else -> ByDateAddedNewestFirst
        }
        set(value) {
            userSharedPreferences.preferredSortType = value.value
            updateCurrentPreferences()
        }

    override var hiddenPostQuickOptions: Set<String>
        get() = userSharedPreferences.hiddenPostQuickOptions
        set(value) {
            userSharedPreferences.hiddenPostQuickOptions = value
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

    override var useSplitNav: Boolean
        get() = userSharedPreferences.useSplitNav
        set(value) {
            userSharedPreferences.useSplitNav = value
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

    override var removeUtmParameters: Boolean
        get() = userSharedPreferences.removeUtmParameters
        set(value) {
            userSharedPreferences.removeUtmParameters = value
            updateCurrentPreferences()
        }

    override var removedUrlParameters: Set<String>
        get() = userSharedPreferences.removedUrlParameters.toSortedSet()
        set(value) {
            userSharedPreferences.removedUrlParameters = value
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

    override var alphabetizeTags: Boolean
        get() = userSharedPreferences.alphabetizeTags
        set(value) {
            userSharedPreferences.alphabetizeTags = value
            updateCurrentPreferences()
        }

    private fun getUserCredentials(): UserCredentials = UserCredentials(
        pinboardAuthToken = userSharedPreferences.pinboardAuthToken,
        linkdingInstanceUrl = linkdingInstanceUrl,
        linkdingAuthToken = userSharedPreferences.linkdingAuthToken,
        appReviewMode = userSharedPreferences.appReviewMode,
    )

    private fun updateCurrentPreferences() {
        _currentPreferences.value = getPreferences()
    }

    private fun getPreferences(): UserPreferences = UserPreferences(
        periodicSync = periodicSync,
        appearance = appearance,
        applyDynamicColors = applyDynamicColors,
        disableScreenshots = disableScreenshots,
        preferredDateFormat = preferredDateFormat,
        preferredSortType = preferredSortType,
        hiddenPostQuickOptions = hiddenPostQuickOptions,
        preferredDetailsView = preferredDetailsView,
        useSplitNav = useSplitNav,
        followRedirects = followRedirects,
        removeUtmParameters = removeUtmParameters,
        removedUrlParameters = removedUrlParameters,
        autoFillDescription = autoFillDescription,
        useBlockquote = useBlockquote,
        showDescriptionInLists = showDescriptionInLists,
        defaultPrivate = defaultPrivate ?: false,
        defaultReadLater = defaultReadLater ?: false,
        editAfterSharing = editAfterSharing,
        defaultTags = defaultTags,
        alphabetizeTags = alphabetizeTags,
    )

    override fun setAuthToken(appMode: AppMode, authToken: String) {
        when {
            "app_review_mode" == authToken -> {
                userSharedPreferences.appReviewMode = true
                userSharedPreferences.pinboardAuthToken = null
                userSharedPreferences.linkdingAuthToken = null
            }

            AppMode.PINBOARD == appMode -> userSharedPreferences.pinboardAuthToken = authToken.ifBlank { null }

            AppMode.LINKDING == appMode -> userSharedPreferences.linkdingAuthToken = authToken.ifBlank { null }

            else -> Unit
        }

        _userCredentials.update { getUserCredentials() }
    }

    override fun clearAuthToken(appMode: AppMode) {
        when (appMode) {
            AppMode.PINBOARD -> {
                userSharedPreferences.pinboardAuthToken = null
            }

            AppMode.LINKDING -> {
                userSharedPreferences.linkdingInstanceUrl = null
                userSharedPreferences.linkdingAuthToken = null
            }

            AppMode.NO_API -> {
                userSharedPreferences.appReviewMode = false
            }

            else -> Unit
        }

        userSharedPreferences.lastUpdate = ""

        _userCredentials.update { getUserCredentials() }
    }
}
