package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Delete: ImageVector
    get() {
        if (_Delete != null) {
            return _Delete!!
        }
        _Delete = ImageVector.Builder(
            name = "Delete",
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
                moveTo(18f, 6f)
                lineTo(17.199f, 18.013f)
                curveTo(17.129f, 19.065f, 17.094f, 19.591f, 16.867f, 19.99f)
                curveTo(16.667f, 20.341f, 16.365f, 20.624f, 16.001f, 20.8f)
                curveTo(15.588f, 21f, 15.061f, 21f, 14.006f, 21f)
                horizontalLineTo(9.994f)
                curveTo(8.939f, 21f, 8.412f, 21f, 7.999f, 20.8f)
                curveTo(7.635f, 20.624f, 7.333f, 20.341f, 7.133f, 19.99f)
                curveTo(6.906f, 19.591f, 6.871f, 19.065f, 6.801f, 18.013f)
                lineTo(6f, 6f)
                moveTo(4f, 6f)
                horizontalLineTo(20f)
                moveTo(16f, 6f)
                lineTo(15.729f, 5.188f)
                curveTo(15.467f, 4.401f, 15.336f, 4.008f, 15.093f, 3.717f)
                curveTo(14.878f, 3.46f, 14.602f, 3.261f, 14.29f, 3.139f)
                curveTo(13.938f, 3f, 13.523f, 3f, 12.694f, 3f)
                horizontalLineTo(11.306f)
                curveTo(10.477f, 3f, 10.062f, 3f, 9.71f, 3.139f)
                curveTo(9.398f, 3.261f, 9.122f, 3.46f, 8.907f, 3.717f)
                curveTo(8.664f, 4.008f, 8.533f, 4.401f, 8.271f, 5.188f)
                lineTo(8f, 6f)
            }
        }.build()

        return _Delete!!
    }

@Suppress("ObjectPropertyName")
private var _Delete: ImageVector? = null
