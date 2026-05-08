package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Help: ImageVector
    get() {
        if (_Help != null) {
            return _Help!!
        }
        _Help = ImageVector.Builder(
            name = "Help",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            ) {
                moveTo(11.967f, 12.75f)
                curveTo(12.967f, 11.75f, 13.967f, 11.355f, 13.967f, 10.25f)
                curveTo(13.967f, 9.145f, 13.072f, 8.25f, 11.967f, 8.25f)
                curveTo(11.035f, 8.25f, 10.252f, 8.887f, 10.03f, 9.75f)
                moveTo(11.967f, 15.75f)
                horizontalLineTo(11.977f)
                moveTo(21f, 12f)
                curveTo(21f, 16.971f, 16.971f, 21f, 12f, 21f)
                curveTo(7.029f, 21f, 3f, 16.971f, 3f, 12f)
                curveTo(3f, 7.029f, 7.029f, 3f, 12f, 3f)
                curveTo(16.971f, 3f, 21f, 7.029f, 21f, 12f)
                close()
            }
        }.build()

        return _Help!!
    }

@Suppress("ObjectPropertyName")
private var _Help: ImageVector? = null
