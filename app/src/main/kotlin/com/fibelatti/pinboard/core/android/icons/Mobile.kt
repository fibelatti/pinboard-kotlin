package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Mobile: ImageVector
    get() {
        if (_Mobile != null) {
            return _Mobile!!
        }
        _Mobile = ImageVector.Builder(
            name = "Mobile",
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
                moveTo(12f, 18f)
                horizontalLineTo(12.01f)
                moveTo(9.2f, 21f)
                horizontalLineTo(14.8f)
                curveTo(15.92f, 21f, 16.48f, 21f, 16.908f, 20.782f)
                curveTo(17.284f, 20.59f, 17.59f, 20.284f, 17.782f, 19.908f)
                curveTo(18f, 19.48f, 18f, 18.92f, 18f, 17.8f)
                verticalLineTo(6.2f)
                curveTo(18f, 5.08f, 18f, 4.52f, 17.782f, 4.092f)
                curveTo(17.59f, 3.716f, 17.284f, 3.41f, 16.908f, 3.218f)
                curveTo(16.48f, 3f, 15.92f, 3f, 14.8f, 3f)
                horizontalLineTo(9.2f)
                curveTo(8.08f, 3f, 7.52f, 3f, 7.092f, 3.218f)
                curveTo(6.716f, 3.41f, 6.41f, 3.716f, 6.218f, 4.092f)
                curveTo(6f, 4.52f, 6f, 5.08f, 6f, 6.2f)
                verticalLineTo(17.8f)
                curveTo(6f, 18.92f, 6f, 19.48f, 6.218f, 19.908f)
                curveTo(6.41f, 20.284f, 6.716f, 20.59f, 7.092f, 20.782f)
                curveTo(7.52f, 21f, 8.08f, 21f, 9.2f, 21f)
                close()
            }
        }.build()

        return _Mobile!!
    }

@Suppress("ObjectPropertyName")
private var _Mobile: ImageVector? = null
