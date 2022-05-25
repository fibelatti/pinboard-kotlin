package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.getOrNull
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.sync.PeriodicSync
import com.fibelatti.pinboard.features.sync.PeriodicSyncManager
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserPreferences
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val getSuggestedTags: GetSuggestedTags,
    private val periodicSyncManager: PeriodicSyncManager,
) : BaseViewModel() {

    val currentPreferences: Flow<UserPreferences> get() = userRepository.currentPreferences

    val appearanceChanged: Flow<Appearance> get() = currentPreferences.map { it.appearance }

    val suggestedTags: Flow<List<String>> get() = _suggestedTags.filterNotNull()
    private val _suggestedTags = MutableStateFlow<List<String>?>(null)

    fun saveAutoUpdate(value: Boolean) {
        userRepository.autoUpdate = value
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

    fun savePreferredDateFormat(preferredDateFormat: PreferredDateFormat) {
        userRepository.preferredDateFormat = preferredDateFormat
    }

    fun savePreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        userRepository.preferredDetailsView = preferredDetailsView
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
        launch {
            _suggestedTags.value = getSuggestedTags(GetSuggestedTags.Params(tag, currentTags)).getOrNull()
        }
    }
}
