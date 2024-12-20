@file:Suppress("Unused")

package com.fibelatti.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
public fun Dp.dpToPx(): Float = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
public fun Int.pxToDp(): Dp = with(LocalDensity.current) { this@pxToDp.toDp() }
