package com.fibelatti.ui.foundation

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

public object Shapes {

    private val mediumCornerSize: CornerSize = CornerSize(16.dp)
    private val smallCornerSize: CornerSize = CornerSize(4.dp)

    public val TopShape: Shape = RoundedCornerShape(
        topStart = mediumCornerSize,
        topEnd = mediumCornerSize,
        bottomStart = smallCornerSize,
        bottomEnd = smallCornerSize,
    )

    public val BottomShape: Shape = RoundedCornerShape(
        topStart = smallCornerSize,
        topEnd = smallCornerSize,
        bottomStart = mediumCornerSize,
        bottomEnd = mediumCornerSize,
    )

    public val StartShape: Shape = RoundedCornerShape(
        topStart = mediumCornerSize,
        topEnd = smallCornerSize,
        bottomStart = mediumCornerSize,
        bottomEnd = smallCornerSize,
    )

    public val EndShape: Shape = RoundedCornerShape(
        topStart = smallCornerSize,
        topEnd = mediumCornerSize,
        bottomStart = smallCornerSize,
        bottomEnd = mediumCornerSize,
    )

    public val MiddleShape: Shape = RoundedCornerShape(smallCornerSize)

    public val StandaloneShape: Shape = RoundedCornerShape(mediumCornerSize)
}
