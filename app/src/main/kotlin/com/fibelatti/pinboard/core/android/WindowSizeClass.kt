package com.fibelatti.pinboard.core.android

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun getWindowSizeClass(): WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
