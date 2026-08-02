package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.SwitchHorizontal: ImageVector
    get() {
        if (_SwitchHorizontal != null) {
            return _SwitchHorizontal!!
        }
        _SwitchHorizontal = ImageVector.Builder(
            name = "SwitchHorizontal",
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
                moveTo(18f, 10f)
                lineTo(21f, 7f)
                moveTo(21f, 7f)
                lineTo(18f, 4f)
                moveTo(21f, 7f)
                horizontalLineTo(7f)
                moveTo(6f, 14f)
                lineTo(3f, 17f)
                moveTo(3f, 17f)
                lineTo(6f, 20f)
                moveTo(3f, 17f)
                horizontalLineTo(17f)
            }
        }.build()

        return _SwitchHorizontal!!
    }

@Suppress("ObjectPropertyName")
private var _SwitchHorizontal: ImageVector? = null
