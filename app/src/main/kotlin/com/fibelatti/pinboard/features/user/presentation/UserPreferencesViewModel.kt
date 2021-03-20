package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.functional.onSuccess
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.PreferredDateFormat
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.posts.domain.usecase.GetSuggestedTags
import com.fibelatti.pinboard.features.tags.domain.model.Tag
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class UserPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
    private val getSuggestedTags: GetSuggestedTags,
) : BaseViewModel() {

    val appearanceChanged: Flow<Appearance>
        get() = _appearanceChanged.filterNotNull()
    private val _appearanceChanged = MutableStateFlow<Appearance?>(null)

    val suggestedTags: Flow<List<String>> get() = _suggestedTags.filterNotNull()
    private val _suggestedTags = MutableStateFlow<List<String>?>(null)

    fun saveAppearance(appearance: Appearance) {
        launch {
            userRepository.setAppearance(appearance)
            appStateRepository.reset()
            _appearanceChanged.value = appearance
        }
    }

    fun savePreferredDateFormat(preferredDateFormat: PreferredDateFormat) {
        launch {
            userRepository.preferredDateFormat = preferredDateFormat
        }
    }

    fun savePreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        launch {
            userRepository.setPreferredDetailsView(preferredDetailsView)
        }
    }

    fun saveMarkAsReadOnOpen(value: Boolean) {
        launch {
            userRepository.setMarkAsReadOnOpen(value)
        }
    }

    fun saveAutoFillDescription(value: Boolean) {
        launch {
            userRepository.setAutoFillDescription(value)
        }
    }

    fun saveShowDescriptionInLists(value: Boolean) {
        launch {
            userRepository.setShowDescriptionInLists(value)
        }
    }

    fun saveEditAfterSharing(editAfterSharing: EditAfterSharing) {
        launch {
            userRepository.setEditAfterSharing(editAfterSharing)
        }
    }

    fun saveDefaultPrivate(value: Boolean) {
        launch {
            userRepository.setDefaultPrivate(value)
        }
    }

    fun saveDefaultReadLater(value: Boolean) {
        launch {
            userRepository.setDefaultReadLater(value)
        }
    }

    fun saveDefaultTags(tags: List<Tag>) {
        launch {
            userRepository.setDefaultTags(tags)
        }
    }

    fun searchForTag(tag: String, currentTags: List<Tag>) {
        launch {
            getSuggestedTags(GetSuggestedTags.Params(tag, currentTags))
                .onSuccess { _suggestedTags.value = it }
        }
    }
}
