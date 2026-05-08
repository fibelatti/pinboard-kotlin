package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Sync: ImageVector
    get() {
        if (_Sync != null) {
            return _Sync!!
        }
        _Sync = ImageVector.Builder(
            name = "Sync",
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
                moveTo(21f, 3f)
                verticalLineTo(8f)
                moveTo(21f, 8f)
                horizontalLineTo(16f)
                moveTo(21f, 8f)
                lineTo(18f, 5.292f)
                curveTo(16.408f, 3.867f, 14.305f, 3f, 12f, 3f)
                curveTo(7.029f, 3f, 3f, 7.029f, 3f, 12f)
                curveTo(3f, 16.971f, 7.029f, 21f, 12f, 21f)
                curveTo(16.283f, 21f, 19.868f, 18.008f, 20.777f, 14f)
            }
        }.build()

        return _Sync!!
    }

@Suppress("ObjectPropertyName")
private var _Sync: ImageVector? = null
