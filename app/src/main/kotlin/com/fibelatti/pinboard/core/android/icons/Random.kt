package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Random: ImageVector
    get() {
        if (_Random != null) {
            return _Random!!
        }
        _Random = ImageVector.Builder(
            name = "Random",
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
                moveTo(18f, 4f)
                lineTo(21f, 7f)
                moveTo(21f, 7f)
                lineTo(18f, 10f)
                moveTo(21f, 7f)
                horizontalLineTo(17f)
                curveTo(16.071f, 7f, 15.606f, 7f, 15.22f, 7.077f)
                curveTo(13.633f, 7.392f, 12.392f, 8.633f, 12.077f, 10.22f)
                curveTo(12f, 10.606f, 12f, 11.071f, 12f, 12f)
                curveTo(12f, 12.929f, 12f, 13.394f, 11.923f, 13.78f)
                curveTo(11.608f, 15.367f, 10.367f, 16.608f, 8.78f, 16.923f)
                curveTo(8.394f, 17f, 7.929f, 17f, 7f, 17f)
                horizontalLineTo(3f)
                moveTo(18f, 20f)
                lineTo(21f, 17f)
                moveTo(21f, 17f)
                lineTo(18f, 14f)
                moveTo(21f, 17f)
                horizontalLineTo(17f)
                curveTo(16.071f, 17f, 15.606f, 17f, 15.22f, 16.923f)
                curveTo(15.146f, 16.908f, 15.072f, 16.892f, 15f, 16.873f)
                moveTo(3f, 7f)
                horizontalLineTo(7f)
                curveTo(7.929f, 7f, 8.394f, 7f, 8.78f, 7.077f)
                curveTo(8.854f, 7.092f, 8.928f, 7.108f, 9f, 7.127f)
            }
        }.build()

        return _Random!!
    }

@Suppress("ObjectPropertyName")
private var _Random: ImageVector? = null
