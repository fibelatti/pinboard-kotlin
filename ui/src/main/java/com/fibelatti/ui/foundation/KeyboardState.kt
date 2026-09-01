package com.fibelatti.ui.foundation

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

@Composable
@OptIn(ExperimentalLayoutApi::class)
public fun rememberKeyboardState(): State<KeyboardState> {
    val density: Density = LocalDensity.current
    val imeInsets: WindowInsets = WindowInsets.ime
    val imeTarget: WindowInsets = WindowInsets.imeAnimationTarget

    return remember(density, imeInsets, imeTarget) {
        derivedStateOf {
            keyboardState(
                current = imeInsets.getBottom(density),
                target = imeTarget.getBottom(density),
            )
        }
    }
}

private fun keyboardState(current: Int, target: Int): KeyboardState = when {
    current == target -> if (target > 0) KeyboardState.OPEN else KeyboardState.CLOSED
    target > 0 -> KeyboardState.OPENING
    else -> KeyboardState.CLOSING
}

public enum class KeyboardState {
    CLOSED,
    OPENING,
    OPEN,
    CLOSING,
    ;

    public val isOpen: Boolean get() = this == OPEN
    public val isClosed: Boolean get() = this == CLOSED
}
