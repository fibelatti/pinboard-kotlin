package com.fibelatti.pinboard.core

import com.fibelatti.pinboard.BuildConfig
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.user.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@Singleton
class AppModeProvider @Inject constructor(
    private val userRepository: UserRepository,
    @Scope(AppDispatchers.DEFAULT) scope: CoroutineScope,
    sharingStarted: SharingStarted,
) {

    val appMode: StateFlow<AppMode> = combine(
        userRepository.currentPreferences,
        userRepository.authToken,
    ) { preferences, authToken -> getValue(preferences.useLinkding, authToken) }
        .stateIn(scope, sharingStarted, getValue())

    @Suppress("KotlinConstantConditions")
    private fun getValue(
        useLinkding: Boolean = userRepository.useLinkding,
        authToken: String = userRepository.authToken.value,
    ): AppMode = when {
        BuildConfig.FLAVOR == "noapi" || authToken == "app_review_mode" -> AppMode.NO_API
        authToken.isBlank() -> AppMode.UNSET
        useLinkding -> AppMode.LINKDING
        else -> AppMode.PINBOARD
    }
}
