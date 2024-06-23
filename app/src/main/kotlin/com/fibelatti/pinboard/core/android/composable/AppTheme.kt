package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.bookmarking.features.user.domain.UserRepository
import com.fibelatti.ui.theme.ExtendedTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.android.annotation.KoinViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppTheme(
    pinboardThemeViewModel: AppThemeViewModel = koinViewModel(),
    content: @Composable () -> Unit,
) {
    val state by pinboardThemeViewModel.applyDynamicColors.collectAsStateWithLifecycle()

    ExtendedTheme(dynamicColor = state, content = content)
}

@KoinViewModel
class AppThemeViewModel(
    userRepository: UserRepository,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : ViewModel() {

    val applyDynamicColors: StateFlow<Boolean> = userRepository.currentPreferences
        .map { it.applyDynamicColors }
        .stateIn(scope, sharingStarted, userRepository.applyDynamicColors)
}
