package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.user.domain.UserCredentials
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber

@Singleton
class AppModeProvider @Inject constructor(
    private val userRepository: UserRepository,
    @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
    sharingStarted: SharingStarted,
) {

    private val userSelection: MutableStateFlow<AppMode?> = MutableStateFlow(null)

    val appMode: StateFlow<AppMode> = combine(userRepository.userCredentials, userSelection, ::getValue)
        .onEach { Timber.d("App mode: $it") }
        .flowOn(dispatcher)
        .stateIn(scope = scope, started = sharingStarted, initialValue = getValue())

    suspend fun setSelection(appMode: AppMode?) {
        Timber.d("Setting app mode: $appMode")
        userSelection.update { appMode }

        // Wait until `setSelection` takes effect before retuning
        when {
            userRepository.userCredentials.first().appReviewMode -> {
                this.appMode.first { it == AppMode.NO_API }
            }

            appMode != null -> {
                this.appMode.first { it == appMode }
            }
        }
    }

    private fun getValue(
        credentials: UserCredentials = userRepository.userCredentials.value,
        selection: AppMode? = this.userSelection.value,
    ): AppMode {
        Timber.d("Determining app mode (credentials=$credentials, userSelection=$selection)")
        return when {
            credentials.appReviewMode -> AppMode.NO_API

            !credentials.pinboardAuthToken.isNullOrBlank() && (selection == AppMode.PINBOARD || selection == null) -> {
                AppMode.PINBOARD
            }

            !credentials.linkdingAuthToken.isNullOrBlank() && (selection == AppMode.LINKDING || selection == null) -> {
                AppMode.LINKDING
            }

            else -> AppMode.UNSET
        }
    }
}
