package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val AppIcons.Search: ImageVector
    get() {
        _Search?.let { return it }

        return ImageVector.Builder(
            name = "Search",
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
                moveTo(11f, 6f)
                curveTo(13.761f, 6f, 16f, 8.239f, 16f, 11f)
                moveTo(16.659f, 16.655f)
                lineTo(21f, 21f)
                moveTo(19f, 11f)
                curveTo(19f, 15.418f, 15.418f, 19f, 11f, 19f)
                curveTo(6.582f, 19f, 3f, 15.418f, 3f, 11f)
                curveTo(3f, 6.582f, 6.582f, 3f, 11f, 3f)
                curveTo(15.418f, 3f, 19f, 6.582f, 19f, 11f)
                close()
            }
        }.build().also { _Search = it }
    }

@Suppress("ObjectPropertyName")
private var _Search: ImageVector? = null
