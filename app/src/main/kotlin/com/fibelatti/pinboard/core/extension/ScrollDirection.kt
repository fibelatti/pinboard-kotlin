package com.fibelatti.pinboard.core.extension

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.mapLatest

enum class ScrollDirection {
    IDLE,
    UP,
    DOWN,
}

@Composable
fun LazyListState.rememberScrollDirection(autoResetTime: Long = 2_000): State<ScrollDirection> {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

    val isScrolling by snapshotFlow { isScrollInProgress }
        .mapLatest { scrolling ->
            if (!scrolling) delay(autoResetTime)
            scrolling
        }
        .collectAsState(isScrollInProgress)

    return remember(this) {
        derivedStateOf {
            when {
                isScrolling && previousIndex != firstVisibleItemIndex && previousIndex < firstVisibleItemIndex -> {
                    ScrollDirection.DOWN
                }

                isScrolling && previousIndex != firstVisibleItemIndex -> {
                    ScrollDirection.UP
                }

                isScrolling && previousScrollOffset < firstVisibleItemScrollOffset -> {
                    ScrollDirection.DOWN
                }

                isScrolling && previousScrollOffset > firstVisibleItemScrollOffset -> {
                    ScrollDirection.UP
                }

                else -> ScrollDirection.IDLE
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }
}

@Composable
fun View.rememberScrollDirection(): State<ScrollDirection> {
    var nestedScrollDirection by remember { mutableStateOf(ScrollDirection.IDLE) }

    val scrollChangeListener = object : ViewTreeObserver.OnScrollChangedListener {
        private var lastScrollY = 0

        override fun onScrollChanged() {
            if (abs(scrollY - lastScrollY) < 100) return

            nestedScrollDirection = (if (scrollY > lastScrollY) ScrollDirection.DOWN else ScrollDirection.UP)
            lastScrollY = scrollY
        }
    }

    DisposableEffect(this) {
        viewTreeObserver.addOnScrollChangedListener(scrollChangeListener)

        onDispose {
            viewTreeObserver.removeOnScrollChangedListener(scrollChangeListener)
        }
    }

    return remember(this) {
        derivedStateOf { nestedScrollDirection }
    }
}
