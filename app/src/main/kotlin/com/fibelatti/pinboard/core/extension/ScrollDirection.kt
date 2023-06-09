package com.fibelatti.pinboard.core.extension

import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.math.abs

enum class ScrollDirection {
    IDLE, UP, DOWN,
}

@Composable
fun LazyListState.rememberScrollDirection(): State<ScrollDirection> {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }

    return remember(this) {
        derivedStateOf {
            when {
                previousIndex != firstVisibleItemIndex && previousIndex < firstVisibleItemIndex -> {
                    ScrollDirection.DOWN
                }

                previousIndex != firstVisibleItemIndex -> {
                    ScrollDirection.UP
                }

                previousScrollOffset < firstVisibleItemScrollOffset -> {
                    ScrollDirection.DOWN
                }

                previousScrollOffset > firstVisibleItemScrollOffset -> {
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

    LaunchedEffect(this) {
        val scrollChangeListener = object : ViewTreeObserver.OnScrollChangedListener {
            private var lastScrollY = 0

            override fun onScrollChanged() {
                if (abs(scrollY - lastScrollY) < 100) return

                nestedScrollDirection = if (scrollY > lastScrollY) {
                    ScrollDirection.DOWN
                } else {
                    ScrollDirection.UP
                }

                lastScrollY = scrollY
            }
        }

        viewTreeObserver.addOnScrollChangedListener(scrollChangeListener)
    }

    return remember(this) {
        derivedStateOf { nestedScrollDirection }
    }
}
