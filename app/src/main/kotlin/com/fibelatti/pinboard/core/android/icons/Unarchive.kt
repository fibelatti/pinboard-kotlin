package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Unarchive: ImageVector
    get() {
        if (_Unarchive != null) {
            return _Unarchive!!
        }
        _Unarchive = ImageVector.Builder(
            name = "Unarchive",
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
                // Lid
                moveTo(3f, 4f)
                horizontalLineTo(21f)
                verticalLineTo(8f)
                horizontalLineTo(3f)
                close()
                // Body
                moveTo(5f, 8f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(8f)
                // Up arrow
                moveTo(12f, 16f)
                verticalLineTo(11f)
                moveTo(9.5f, 13.5f)
                lineTo(12f, 11f)
                lineTo(14.5f, 13.5f)
            }
        }.build()

        return _Unarchive!!
    }

@Suppress("ObjectPropertyName")
private var _Unarchive: ImageVector? = null
