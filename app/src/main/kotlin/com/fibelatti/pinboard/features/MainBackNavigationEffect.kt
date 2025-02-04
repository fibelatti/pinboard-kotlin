package com.fibelatti.pinboard.features

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.fibelatti.core.android.extension.navigateBack
import com.fibelatti.pinboard.core.android.composable.LocalAppCompatActivity
import com.fibelatti.pinboard.core.android.composable.hiltActivityViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Composable
fun MainBackNavigationEffect(
    mainViewModel: MainViewModel = hiltActivityViewModel(),
    actionId: String,
) {
    val localActivity = LocalAppCompatActivity.current
    val localLifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(mainViewModel, actionId) {
        mainViewModel.navigationClicks(actionId)
            .onEach { localActivity.navigateBack() }
            .flowWithLifecycle(localLifecycle)
            .launchIn(this)
    }
}
