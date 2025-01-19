package com.fibelatti.pinboard.core.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp

fun Modifier.fillWidthOfParent(parentPaddingStart: Dp, parentPaddingEnd: Dp): Modifier {
    return this then Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = constraints.maxWidth + parentPaddingStart.roundToPx() + parentPaddingEnd.roundToPx(),
            ),
        )

        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}
