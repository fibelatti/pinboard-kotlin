package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.ui.theme.ExtendedTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Composable
fun AppTheme(
    pinboardThemeViewModel: AppThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by pinboardThemeViewModel.applyDynamicColors.collectAsStateWithLifecycle()

    ExtendedTheme(dynamicColor = state, content = content)
}

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    userRepository: UserRepository,
    scope: CoroutineScope,
    sharingStarted: SharingStarted,
) : ViewModel() {

    val applyDynamicColors: StateFlow<Boolean> = userRepository.currentPreferences
        .map { it.applyDynamicColors }
        .stateIn(scope = scope, started = sharingStarted, initialValue = userRepository.applyDynamicColors)
}
