package com.fibelatti.pinboard.core.android.composable

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun rememberAutoDismissPullRefreshState(
    onPullToRefresh: () -> Unit,
): Pair<PullRefreshState, Boolean> {
    val scope = rememberCoroutineScope()
    var refreshing by rememberSaveable { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onPullToRefresh()
                delay(500L)
                refreshing = false
            }
        },
    )

    return pullRefreshState to refreshing
}
