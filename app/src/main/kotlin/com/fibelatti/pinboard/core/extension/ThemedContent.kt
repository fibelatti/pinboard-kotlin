package com.fibelatti.pinboard.core.extension

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.platform.ComposeView
import com.fibelatti.pinboard.core.android.composable.AppTheme

fun ComposeView.setThemedContent(content: @Composable () -> Unit) {
    setContent {
        AppTheme(content = content)
    }
}

fun ComponentActivity.setThemedContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
) {
    setContent(parent) {
        AppTheme(content = content)
    }
}
