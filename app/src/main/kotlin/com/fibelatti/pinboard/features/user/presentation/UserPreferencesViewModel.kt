package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.EditAfterSharing
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class UserPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository,
) : BaseViewModel() {

    val appearanceChanged: Flow<Appearance>
        get() = _appearanceChanged.filterNotNull()
    private val _appearanceChanged = MutableStateFlow<Appearance?>(null)

    fun saveAppearance(appearance: Appearance) {
        launch {
            userRepository.setAppearance(appearance)
            appStateRepository.reset()
            _appearanceChanged.value = appearance
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
}
