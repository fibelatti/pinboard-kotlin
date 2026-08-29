package com.fibelatti.pinboard.core.android.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.fibelatti.pinboard.core.di.AppDispatchers
import com.fibelatti.pinboard.core.di.Scope
import com.fibelatti.pinboard.features.user.domain.UserRepository
import com.fibelatti.ui.theme.ExtendedTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

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
    @Scope(AppDispatchers.DEFAULT) dispatcher: CoroutineDispatcher,
    sharingStarted: SharingStarted,
) : ViewModel() {

    private val scope: CoroutineScope = viewModelScope + dispatcher

    val applyDynamicColors: StateFlow<Boolean> = userRepository.currentPreferences
        .map { it.applyDynamicColors }
        .stateIn(scope = scope, started = sharingStarted, initialValue = userRepository.applyDynamicColors)
}
