package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.core.archcomponents.LiveEvent
import com.fibelatti.core.archcomponents.MutableLiveEvent
import com.fibelatti.core.archcomponents.postEvent
import com.fibelatti.pinboard.core.android.Appearance
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.posts.domain.PreferredDetailsView
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val appStateRepository: AppStateRepository
) : BaseViewModel() {

    val appearanceChanged: LiveEvent<Appearance> get() = _appearanceChanged
    private val _appearanceChanged = MutableLiveEvent<Appearance>()

    fun saveAppearance(appearance: Appearance) {
        launch {
            userRepository.setAppearance(appearance)
            appStateRepository.reset()
            _appearanceChanged.postEvent(appearance)
        }
    }

    fun savePreferredDetailsView(preferredDetailsView: PreferredDetailsView) {
        launch {
            userRepository.setPreferredDetailsView(preferredDetailsView)
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

    fun saveShowDescriptionInDetails(value: Boolean) {
        launch {
            userRepository.setShowDescriptionInDetails(value)
        }
    }

    fun saveEditAfterSharing(value: Boolean) {
        launch {
            userRepository.setEditAfterSharing(value)
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
