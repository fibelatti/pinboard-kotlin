package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val AppIcons.Menu: ImageVector
    get() {
        _Menu?.let { return it }

        return ImageVector.Builder(
            name = "Menu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 6f)
                horizontalLineTo(20f)
                moveTo(4f, 12f)
                horizontalLineTo(20f)
                moveTo(4f, 18f)
                horizontalLineTo(20f)
            }
        }.build().also { _Menu = it }
    }

@Suppress("ObjectPropertyName")
private var _Menu: ImageVector? = null
