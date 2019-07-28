package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.core.archcomponents.BaseViewModel
import com.fibelatti.pinboard.features.user.domain.UserRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserPreferencesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

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
