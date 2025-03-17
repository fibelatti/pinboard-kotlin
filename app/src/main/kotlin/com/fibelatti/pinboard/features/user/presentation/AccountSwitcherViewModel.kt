package com.fibelatti.pinboard.features.user.presentation

import com.fibelatti.pinboard.core.AppMode
import com.fibelatti.pinboard.core.android.base.BaseViewModel
import com.fibelatti.pinboard.features.appstate.AddAccount
import com.fibelatti.pinboard.features.appstate.AppStateRepository
import com.fibelatti.pinboard.features.appstate.UserLoggedIn
import com.fibelatti.pinboard.features.appstate.UserLoggedOut
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    scope: CoroutineScope,
    appStateRepository: AppStateRepository,
    userRepository: UserRepository,
) : BaseViewModel(scope, appStateRepository) {

    val userCredentials: StateFlow<UserCredentials> = userRepository.userCredentials

    fun select(appMode: AppMode) {
        runAction(UserLoggedIn(appMode = appMode))
    }

    fun addAccount(appMode: AppMode) {
        runAction(AddAccount(appMode = appMode))
    }

    fun logout(appMode: AppMode) {
        runAction(UserLoggedOut(appMode = appMode))
    }
}
