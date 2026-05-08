package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Copy: ImageVector
    get() {
        if (_Copy != null) {
            return _Copy!!
        }
        _Copy = ImageVector.Builder(
            name = "Copy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(15f, 3f)
                verticalLineTo(6.4f)
                curveTo(15f, 6.96f, 15f, 7.24f, 15.109f, 7.454f)
                curveTo(15.205f, 7.642f, 15.358f, 7.795f, 15.546f, 7.891f)
                curveTo(15.76f, 8f, 16.04f, 8f, 16.6f, 8f)
                horizontalLineTo(20f)
                moveTo(10f, 8f)
                horizontalLineTo(6f)
                curveTo(4.895f, 8f, 4f, 8.895f, 4f, 10f)
                verticalLineTo(19f)
                curveTo(4f, 20.105f, 4.895f, 21f, 6f, 21f)
                horizontalLineTo(12f)
                curveTo(13.105f, 21f, 14f, 20.105f, 14f, 19f)
                verticalLineTo(16f)
                moveTo(16f, 3f)
                horizontalLineTo(13.2f)
                curveTo(12.08f, 3f, 11.52f, 3f, 11.092f, 3.218f)
                curveTo(10.716f, 3.41f, 10.41f, 3.716f, 10.218f, 4.092f)
                curveTo(10f, 4.52f, 10f, 5.08f, 10f, 6.2f)
                verticalLineTo(12.8f)
                curveTo(10f, 13.92f, 10f, 14.48f, 10.218f, 14.908f)
                curveTo(10.41f, 15.284f, 10.716f, 15.59f, 11.092f, 15.782f)
                curveTo(11.52f, 16f, 12.08f, 16f, 13.2f, 16f)
                horizontalLineTo(16.8f)
                curveTo(17.92f, 16f, 18.48f, 16f, 18.908f, 15.782f)
                curveTo(19.284f, 15.59f, 19.59f, 15.284f, 19.782f, 14.908f)
                curveTo(20f, 14.48f, 20f, 13.92f, 20f, 12.8f)
                verticalLineTo(7f)
                lineTo(16f, 3f)
                close()
            }
        }.build()

        return _Copy!!
    }

@Suppress("ObjectPropertyName")
private var _Copy: ImageVector? = null
