package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Filter: ImageVector
    get() {
        if (_Filter != null) {
            return _Filter!!
        }
        _Filter = ImageVector.Builder(
            name = "Filter",
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
                moveTo(21f, 6f)
                horizontalLineTo(19f)
                moveTo(21f, 12f)
                horizontalLineTo(16f)
                moveTo(21f, 18f)
                horizontalLineTo(16f)
                moveTo(7f, 20f)
                verticalLineTo(13.561f)
                curveTo(7f, 13.353f, 7f, 13.249f, 6.98f, 13.15f)
                curveTo(6.961f, 13.061f, 6.932f, 12.976f, 6.891f, 12.896f)
                curveTo(6.844f, 12.805f, 6.779f, 12.724f, 6.649f, 12.562f)
                lineTo(3.351f, 8.438f)
                curveTo(3.221f, 8.276f, 3.156f, 8.195f, 3.109f, 8.104f)
                curveTo(3.068f, 8.024f, 3.039f, 7.939f, 3.02f, 7.85f)
                curveTo(3f, 7.751f, 3f, 7.647f, 3f, 7.439f)
                verticalLineTo(5.6f)
                curveTo(3f, 5.04f, 3f, 4.76f, 3.109f, 4.546f)
                curveTo(3.205f, 4.358f, 3.358f, 4.205f, 3.546f, 4.109f)
                curveTo(3.76f, 4f, 4.04f, 4f, 4.6f, 4f)
                horizontalLineTo(13.4f)
                curveTo(13.96f, 4f, 14.24f, 4f, 14.454f, 4.109f)
                curveTo(14.642f, 4.205f, 14.795f, 4.358f, 14.891f, 4.546f)
                curveTo(15f, 4.76f, 15f, 5.04f, 15f, 5.6f)
                verticalLineTo(7.439f)
                curveTo(15f, 7.647f, 15f, 7.751f, 14.98f, 7.85f)
                curveTo(14.962f, 7.939f, 14.932f, 8.024f, 14.891f, 8.104f)
                curveTo(14.844f, 8.195f, 14.779f, 8.276f, 14.649f, 8.438f)
                lineTo(11.351f, 12.562f)
                curveTo(11.221f, 12.724f, 11.156f, 12.805f, 11.109f, 12.896f)
                curveTo(11.068f, 12.976f, 11.038f, 13.061f, 11.02f, 13.15f)
                curveTo(11f, 13.249f, 11f, 13.353f, 11f, 13.561f)
                verticalLineTo(17f)
                lineTo(7f, 20f)
                close()
            }
        }.build()

        return _Filter!!
    }

@Suppress("ObjectPropertyName")
private var _Filter: ImageVector? = null
