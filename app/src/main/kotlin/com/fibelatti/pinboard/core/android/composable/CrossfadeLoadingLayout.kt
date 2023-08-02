package com.fibelatti.pinboard.core.android.composable

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T : Any?> CrossfadeLoadingLayout(
    data: T?,
    modifier: Modifier = Modifier,
    progressIndicatorSize: Dp = 40.dp,
    progressIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable (T) -> Unit,
) {
    Crossfade(
        targetState = data,
        modifier = modifier,
        label = "CrossfadeLoadingLayout",
    ) {
        if (it == null) {
            LoadingContent(
                progressIndicatorSize = progressIndicatorSize,
                progressIndicatorColor = progressIndicatorColor,
            )
        } else {
            content(it)
        }
    }
}

@Composable
fun LoadingContent(
    progressIndicatorSize: Dp = 40.dp,
    progressIndicatorColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(progressIndicatorSize)
                .align(Alignment.Center),
            color = progressIndicatorColor,
        )
    }
}
