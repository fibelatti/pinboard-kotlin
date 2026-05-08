package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Person: ImageVector
    get() {
        if (_Person != null) {
            return _Person!!
        }
        _Person = ImageVector.Builder(
            name = "Person",
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
                moveTo(16f, 7f)
                curveTo(16f, 9.209f, 14.209f, 11f, 12f, 11f)
                curveTo(9.791f, 11f, 8f, 9.209f, 8f, 7f)
                curveTo(8f, 4.791f, 9.791f, 3f, 12f, 3f)
                curveTo(14.209f, 3f, 16f, 4.791f, 16f, 7f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 14f)
                curveTo(8.134f, 14f, 5f, 17.134f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(19f, 17.134f, 15.866f, 14f, 12f, 14f)
                close()
            }
        }.build()

        return _Person!!
    }

@Suppress("ObjectPropertyName")
private var _Person: ImageVector? = null
