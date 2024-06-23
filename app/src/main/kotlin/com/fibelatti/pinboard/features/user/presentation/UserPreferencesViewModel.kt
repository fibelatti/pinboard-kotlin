package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.bookmarking.core.AppModeProvider
import com.fibelatti.bookmarking.core.ui.Appearance
import com.fibelatti.bookmarking.core.ui.PreferredDateFormat
import com.fibelatti.bookmarking.features.posts.domain.EditAfterSharing
import com.fibelatti.bookmarking.features.posts.domain.PostsRepository
import com.fibelatti.bookmarking.features.posts.domain.PreferredDetailsView
import com.fibelatti.bookmarking.features.sync.PeriodicSync
import com.fibelatti.bookmarking.features.tags.domain.model.Tag
import com.fibelatti.bookmarking.features.user.domain.UserPreferences
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class UserPreferencesViewModel(
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    private val periodicSyncManager: PeriodicSyncManager,
    private val appModeProvider: AppModeProvider,
) : BaseViewModel() {

    val currentPreferences: StateFlow<UserPreferences> get() = userRepository.currentPreferences

    val suggestedTags: Flow<List<String>> get() = _suggestedTags.filterNotNull()
    private val _suggestedTags = MutableStateFlow<List<String>?>(null)

    private var searchJob: Job? = null

    fun useLinkding(value: Boolean) {
        userRepository.useLinkding = value
        appModeProvider.refresh()
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

    fun savePreferredDateFormat(preferredDateFormat: PreferredDateFormat) {
        userRepository.preferredDateFormat = preferredDateFormat
    }

    fun savePreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        userRepository.preferredDetailsView = preferredDetailsView
    }

    fun saveAlwaysUseSidePanel(value: Boolean) {
        userRepository.alwaysUseSidePanel = value
    }

    fun saveMarkAsReadOnOpen(value: Boolean) {
        userRepository.markAsReadOnOpen = value
    }

    fun saveAutoFillDescription(value: Boolean) {
        userRepository.autoFillDescription = value
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

    fun saveDefaultTags(tags: List<Tag>) {
        userRepository.defaultTags = tags
    }

    fun searchForTag(tag: String, currentTags: List<Tag>) {
        if (searchJob?.isActive == true) searchJob?.cancel()

        searchJob = launch {
            _suggestedTags.value = postsRepository.searchExistingPostTag(
                tag = tag,
                currentTags = currentTags,
            ).getOrNull()
        }
    }
}
