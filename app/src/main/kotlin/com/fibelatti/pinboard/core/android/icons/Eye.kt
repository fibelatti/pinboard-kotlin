package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Eye: ImageVector
    get() {
        if (_Eye != null) {
            return _Eye!!
        }
        _Eye = ImageVector.Builder(
            name = "Eye",
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
                moveTo(15.001f, 12f)
                curveTo(15.001f, 13.657f, 13.658f, 15f, 12.001f, 15f)
                curveTo(10.344f, 15f, 9.001f, 13.657f, 9.001f, 12f)
                curveTo(9.001f, 10.343f, 10.344f, 9f, 12.001f, 9f)
                curveTo(13.658f, 9f, 15.001f, 10.343f, 15.001f, 12f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12.001f, 5f)
                curveTo(7.524f, 5f, 3.733f, 7.943f, 2.459f, 12f)
                curveTo(3.733f, 16.057f, 7.524f, 19f, 12.001f, 19f)
                curveTo(16.479f, 19f, 20.269f, 16.057f, 21.543f, 12f)
                curveTo(20.269f, 7.943f, 16.479f, 5f, 12.001f, 5f)
                close()
            }
        }.build()

        return _Eye!!
    }

@Suppress("ObjectPropertyName")
private var _Eye: ImageVector? = null
