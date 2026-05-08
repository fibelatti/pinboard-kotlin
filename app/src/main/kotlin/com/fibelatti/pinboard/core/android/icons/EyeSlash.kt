package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.EyeSlash: ImageVector
    get() {
        if (_EyeSlash != null) {
            return _EyeSlash!!
        }
        _EyeSlash = ImageVector.Builder(
            name = "EyeSlash",
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
                moveTo(2.999f, 3f)
                lineTo(20.999f, 21f)
                moveTo(9.843f, 9.914f)
                curveTo(9.321f, 10.454f, 8.999f, 11.189f, 8.999f, 12f)
                curveTo(8.999f, 13.657f, 10.342f, 15f, 11.999f, 15f)
                curveTo(12.821f, 15f, 13.567f, 14.669f, 14.109f, 14.133f)
                moveTo(6.499f, 6.647f)
                curveTo(4.6f, 7.9f, 3.153f, 9.784f, 2.457f, 12f)
                curveTo(3.731f, 16.057f, 7.522f, 19f, 11.999f, 19f)
                curveTo(13.988f, 19f, 15.841f, 18.419f, 17.399f, 17.418f)
                moveTo(10.999f, 5.049f)
                curveTo(11.328f, 5.017f, 11.662f, 5f, 11.999f, 5f)
                curveTo(16.477f, 5f, 20.267f, 7.943f, 21.541f, 12f)
                curveTo(21.261f, 12.894f, 20.858f, 13.734f, 20.352f, 14.5f)
            }
        }.build()

        return _EyeSlash!!
    }

@Suppress("ObjectPropertyName")
private var _EyeSlash: ImageVector? = null
