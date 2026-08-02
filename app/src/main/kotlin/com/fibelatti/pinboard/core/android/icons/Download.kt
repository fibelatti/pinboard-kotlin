package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("UnusedReceiverParameter")
val AppIcons.Download: ImageVector
    get() {
        _Download?.let { return it }

        return ImageVector.Builder(
            name = "Download",
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
                moveTo(17f, 17f)
                horizontalLineTo(17.01f)
                moveTo(17.4f, 14f)
                horizontalLineTo(18f)
                curveTo(18.932f, 14f, 19.398f, 14f, 19.765f, 14.152f)
                curveTo(20.255f, 14.355f, 20.645f, 14.745f, 20.848f, 15.235f)
                curveTo(21f, 15.602f, 21f, 16.068f, 21f, 17f)
                curveTo(21f, 17.932f, 21f, 18.398f, 20.848f, 18.765f)
                curveTo(20.645f, 19.255f, 20.255f, 19.645f, 19.765f, 19.848f)
                curveTo(19.398f, 20f, 18.932f, 20f, 18f, 20f)
                horizontalLineTo(6f)
                curveTo(5.068f, 20f, 4.602f, 20f, 4.235f, 19.848f)
                curveTo(3.745f, 19.645f, 3.355f, 19.255f, 3.152f, 18.765f)
                curveTo(3f, 18.398f, 3f, 17.932f, 3f, 17f)
                curveTo(3f, 16.068f, 3f, 15.602f, 3.152f, 15.235f)
                curveTo(3.355f, 14.745f, 3.745f, 14.355f, 4.235f, 14.152f)
                curveTo(4.602f, 14f, 5.068f, 14f, 6f, 14f)
                horizontalLineTo(6.6f)
                moveTo(12f, 15f)
                verticalLineTo(4f)
                moveTo(12f, 15f)
                lineTo(9f, 12f)
                moveTo(12f, 15f)
                lineTo(15f, 12f)
            }
        }.build().also { _Download = it }
    }

@Suppress("ObjectPropertyName")
private var _Download: ImageVector? = null
