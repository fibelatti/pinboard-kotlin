package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.icons.AppIcons
import com.fibelatti.pinboard.core.android.icons.ChevronTop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PullRefreshLayout(
    onPullToRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    autoShowScrollToTop: Boolean = true,
    scrollToTopPadding: Dp = 0.dp,
    content: LazyListScope.() -> Unit,
) {
    val scope: CoroutineScope = rememberCoroutineScope()
    var refreshing: Boolean by rememberSaveable { mutableStateOf(false) }
    val showScrollToTop: Boolean by remember(autoShowScrollToTop) {
        derivedStateOf { autoShowScrollToTop && listState.firstVisibleItemIndex > 5 }
    }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onPullToRefresh()
                delay(timeMillis = 500L)
                refreshing = false
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            content = content,
        )

        AnimatedVisibility(
            visible = showScrollToTop,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            ScrollToTopButton(
                onClick = {
                    scope.launch {
                        if (listState.firstVisibleItemIndex > 20) {
                            listState.scrollToItem(index = 0)
                        } else {
                            listState.animateScrollToItem(index = 0)
                        }
                    }
                },
                modifier = Modifier
                    .padding(all = 8.dp)
                    .padding(top = scrollToTopPadding),
            )
        }
    }
}

@Composable
private fun ScrollToTopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LongClickIconButton(
        painter = rememberVectorPainter(AppIcons.ChevronTop),
        description = stringResource(id = R.string.cd_scroll_to_top),
        onClick = onClick,
        modifier = modifier
            .widthIn(min = 100.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.large,
            ),
        iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}
