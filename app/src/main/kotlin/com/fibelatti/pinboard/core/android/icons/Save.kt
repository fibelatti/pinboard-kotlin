package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Save: ImageVector
    get() {
        if (_Save != null) {
            return _Save!!
        }
        _Save = ImageVector.Builder(
            name = "Save",
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
                moveTo(8f, 3.002f)
                curveTo(8.065f, 3.001f, 8.131f, 3.001f, 8.2f, 3.001f)
                horizontalLineTo(13.462f)
                curveTo(14.027f, 3.001f, 14.309f, 3.001f, 14.57f, 3.073f)
                curveTo(14.72f, 3.115f, 14.864f, 3.173f, 15f, 3.248f)
                moveTo(8f, 3.002f)
                curveTo(7.012f, 3.002f, 6.494f, 3.015f, 6.092f, 3.219f)
                curveTo(5.716f, 3.411f, 5.41f, 3.717f, 5.218f, 4.093f)
                curveTo(5f, 4.521f, 5f, 5.081f, 5f, 6.201f)
                verticalLineTo(17.802f)
                curveTo(5f, 18.922f, 5f, 19.482f, 5.218f, 19.909f)
                curveTo(5.41f, 20.286f, 5.716f, 20.592f, 6.092f, 20.784f)
                curveTo(6.52f, 21.001f, 7.08f, 21.001f, 8.2f, 21.001f)
                horizontalLineTo(15.8f)
                curveTo(16.92f, 21.001f, 17.48f, 21.001f, 17.908f, 20.784f)
                curveTo(18.284f, 20.592f, 18.59f, 20.286f, 18.782f, 19.909f)
                curveTo(19f, 19.482f, 19f, 18.922f, 19f, 17.802f)
                verticalLineTo(9.124f)
                curveTo(19f, 8.708f, 19f, 8.5f, 18.959f, 8.301f)
                curveTo(18.923f, 8.124f, 18.863f, 7.954f, 18.781f, 7.793f)
                curveTo(18.689f, 7.612f, 18.559f, 7.45f, 18.299f, 7.125f)
                lineTo(15.961f, 4.202f)
                curveTo(15.608f, 3.761f, 15.432f, 3.541f, 15.213f, 3.382f)
                curveTo(15.144f, 3.333f, 15.073f, 3.288f, 15f, 3.248f)
                moveTo(8f, 3.002f)
                verticalLineTo(7f)
                horizontalLineTo(15f)
                verticalLineTo(3.248f)
                moveTo(15f, 15f)
                curveTo(15f, 16.657f, 13.657f, 18f, 12f, 18f)
                curveTo(10.343f, 18f, 9f, 16.657f, 9f, 15f)
                curveTo(9f, 13.343f, 10.343f, 12f, 12f, 12f)
                curveTo(13.657f, 12f, 15f, 13.343f, 15f, 15f)
                close()
            }
        }.build()

        return _Save!!
    }

@Suppress("ObjectPropertyName")
private var _Save: ImageVector? = null
