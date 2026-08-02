package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserPreferencesContent
import com.fibelatti.pinboard.features.offline.domain.OfflineCopyRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.tags.domain.TagManagerRepository
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    private val userRepository: UserRepository,
    private val tagManagerRepository: TagManagerRepository,
    private val periodicSyncManager: PeriodicSyncManager,
    private val offlineCopyRepository: OfflineCopyRepository,
) : BaseViewModel(scope, appStateRepository), TagManagerRepository by tagManagerRepository {

    val userCredentials: StateFlow<UserCredentials> get() = userRepository.userCredentials

    val currentPreferences: StateFlow<UserPreferences> get() = userRepository.currentPreferences

    private val _offlineCopiesSize: MutableStateFlow<Long> = MutableStateFlow(0L)
    val offlineCopiesSize: StateFlow<Long> = _offlineCopiesSize.asStateFlow()

    init {
        scope.launch {
            appStateRepository.appState
                .filter { it.content is UserPreferencesContent }
                .collectLatest { _offlineCopiesSize.value = offlineCopyRepository.totalSizeOnDisk() }
        }

        scope.launch {
            tagManagerState.combine(appStateRepository.appState) { tagManagerState, appState ->
                tagManagerState.takeIf { appState.content is UserPreferencesContent }
            }.filterNotNull().collectLatest { value ->
                userRepository.defaultTags = value.tags
            }
        }
    }

    fun clearOfflineCopies() {
        scope.launch {
            offlineCopyRepository.deleteEverything()
            _offlineCopiesSize.value = offlineCopyRepository.totalSizeOnDisk()
        }
    }

    fun savePeriodicSync(periodicSync: PeriodicSync) {
        userRepository.periodicSync = periodicSync
        periodicSyncManager.enqueueWork(shouldReplace = true)
    }

    fun saveAppearance(appearance: Appearance) {
        userRepository.appearance = appearance
    }

    fun saveApplyDynamicColors(value: Boolean) {
        userRepository.applyDynamicColors = value
    }

    fun saveDisableScreenshots(value: Boolean) {
        userRepository.disableScreenshots = value
    }

    fun savePreferredDateFormat(preferredDateFormat: PreferredDateFormat, includeTime: Boolean) {
        val newValue = when (preferredDateFormat) {
            is PreferredDateFormat.DayMonthYearWithTime -> preferredDateFormat.copy(includeTime = includeTime)
            is PreferredDateFormat.MonthDayYearWithTime -> preferredDateFormat.copy(includeTime = includeTime)
            is PreferredDateFormat.ShortYearMonthDayWithTime -> preferredDateFormat.copy(includeTime = includeTime)
            is PreferredDateFormat.YearMonthDayWithTime -> preferredDateFormat.copy(includeTime = includeTime)
            is PreferredDateFormat.NoDate -> preferredDateFormat
        }

        userRepository.preferredDateFormat = newValue
    }

    fun saveHiddenPostQuickOptions(hiddenPostQuickOptions: Set<String>) {
        userRepository.hiddenPostQuickOptions = hiddenPostQuickOptions
    }

    fun savePreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        userRepository.preferredDetailsView = preferredDetailsView
    }

    fun saveUseSplitNav(value: Boolean) {
        userRepository.useSplitNav = value
    }

    fun saveMarkAsReadOnOpen(value: Boolean) {
        userRepository.markAsReadOnOpen = value
    }

    fun saveFollowRedirects(value: Boolean) {
        userRepository.followRedirects = value
    }

    fun saveRemoveUtmParameters(value: Boolean) {
        userRepository.removeUtmParameters = value
    }

    fun saveRemovedUrlParameters(parameters: Set<String>) {
        userRepository.removedUrlParameters = parameters
    }

    fun saveAutoFillDescription(value: Boolean) {
        userRepository.autoFillDescription = value
    }

    fun saveUseBlockquote(value: Boolean) {
        userRepository.useBlockquote = value
    }

    fun saveShowDescriptionInLists(value: Boolean) {
        userRepository.showDescriptionInLists = value
    }

    fun saveEditAfterSharing(editAfterSharing: EditAfterSharing) {
        userRepository.editAfterSharing = editAfterSharing
    }

    fun saveDefaultPrivate(value: Boolean) {
        userRepository.defaultPrivate = value
    }

    fun saveDefaultReadLater(value: Boolean) {
        userRepository.defaultReadLater = value
    }

    fun saveAlphabetizeTags(value: Boolean) {
        userRepository.alphabetizeTags = value
    }

    fun saveUseBackgroundShareReceiver(value: Boolean) {
        userRepository.useBackgroundShareReceiver = value
    }
}
