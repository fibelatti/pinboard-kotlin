package com.fibelatti.ui.foundation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
public fun rememberKeyboardState(): State<Boolean> {
    val density: Density = LocalDensity.current
    val imeInsets: WindowInsets = WindowInsets.ime
    return remember(density, imeInsets) {
        derivedStateOf { imeInsets.getBottom(density) > 0 }
    }
}
