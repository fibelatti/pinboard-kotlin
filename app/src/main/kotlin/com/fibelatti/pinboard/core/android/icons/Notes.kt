package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Notes: ImageVector
    get() {
        if (_Notes != null) {
            return _Notes!!
        }
        _Notes = ImageVector.Builder(
            name = "Notes",
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
                moveTo(9f, 3f)
                verticalLineTo(5f)
                moveTo(12f, 3f)
                verticalLineTo(5f)
                moveTo(15f, 3f)
                verticalLineTo(5f)
                moveTo(13f, 9f)
                horizontalLineTo(9f)
                moveTo(15f, 13f)
                horizontalLineTo(9f)
                moveTo(8.2f, 21f)
                horizontalLineTo(15.8f)
                curveTo(16.92f, 21f, 17.48f, 21f, 17.908f, 20.782f)
                curveTo(18.284f, 20.59f, 18.59f, 20.284f, 18.782f, 19.908f)
                curveTo(19f, 19.48f, 19f, 18.92f, 19f, 17.8f)
                verticalLineTo(7.2f)
                curveTo(19f, 6.08f, 19f, 5.52f, 18.782f, 5.092f)
                curveTo(18.59f, 4.716f, 18.284f, 4.41f, 17.908f, 4.218f)
                curveTo(17.48f, 4f, 16.92f, 4f, 15.8f, 4f)
                horizontalLineTo(8.2f)
                curveTo(7.08f, 4f, 6.52f, 4f, 6.092f, 4.218f)
                curveTo(5.716f, 4.41f, 5.41f, 4.716f, 5.218f, 5.092f)
                curveTo(5f, 5.52f, 5f, 6.08f, 5f, 7.2f)
                verticalLineTo(17.8f)
                curveTo(5f, 18.92f, 5f, 19.48f, 5.218f, 19.908f)
                curveTo(5.41f, 20.284f, 5.716f, 20.59f, 6.092f, 20.782f)
                curveTo(6.52f, 21f, 7.08f, 21f, 8.2f, 21f)
                close()
            }
        }.build()

        return _Notes!!
    }

@Suppress("ObjectPropertyName")
private var _Notes: ImageVector? = null
