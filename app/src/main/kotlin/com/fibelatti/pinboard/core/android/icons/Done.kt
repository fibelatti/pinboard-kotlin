package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Done: ImageVector
    get() {
        if (_Done != null) {
            return _Done!!
        }
        _Done = ImageVector.Builder(
            name = "Done",
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
                moveTo(4f, 12.611f)
                lineTo(8.923f, 17.5f)
                lineTo(20f, 6.5f)
            }
        }.build()

        return _Done!!
    }

@Suppress("ObjectPropertyName")
private var _Done: ImageVector? = null
