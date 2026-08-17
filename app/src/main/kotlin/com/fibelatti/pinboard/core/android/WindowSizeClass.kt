package com.fibelatti.pinboard.core.android

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun getWindowSizeClass(): WindowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
