package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Feedback: ImageVector
    get() {
        if (_Feedback != null) {
            return _Feedback!!
        }
        _Feedback = ImageVector.Builder(
            name = "Feedback",
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
                moveTo(10f, 19f)
                horizontalLineTo(6.2f)
                curveTo(5.08f, 19f, 4.52f, 19f, 4.092f, 18.782f)
                curveTo(3.716f, 18.59f, 3.41f, 18.284f, 3.218f, 17.908f)
                curveTo(3f, 17.48f, 3f, 16.92f, 3f, 15.8f)
                verticalLineTo(8.2f)
                curveTo(3f, 7.08f, 3f, 6.52f, 3.218f, 6.092f)
                curveTo(3.41f, 5.716f, 3.716f, 5.41f, 4.092f, 5.218f)
                curveTo(4.52f, 5f, 5.08f, 5f, 6.2f, 5f)
                horizontalLineTo(17.8f)
                curveTo(18.92f, 5f, 19.48f, 5f, 19.908f, 5.218f)
                curveTo(20.284f, 5.41f, 20.59f, 5.716f, 20.782f, 6.092f)
                curveTo(21f, 6.52f, 21f, 7.08f, 21f, 8.2f)
                verticalLineTo(10f)
                moveTo(20.607f, 8.262f)
                lineTo(15.55f, 11.634f)
                curveTo(14.267f, 12.489f, 13.625f, 12.917f, 12.932f, 13.083f)
                curveTo(12.319f, 13.229f, 11.68f, 13.229f, 11.068f, 13.083f)
                curveTo(10.374f, 12.917f, 9.733f, 12.489f, 8.45f, 11.634f)
                lineTo(3.147f, 8.099f)
                moveTo(14f, 21f)
                lineTo(16.025f, 20.595f)
                curveTo(16.201f, 20.56f, 16.29f, 20.542f, 16.372f, 20.51f)
                curveTo(16.445f, 20.481f, 16.515f, 20.444f, 16.579f, 20.399f)
                curveTo(16.652f, 20.348f, 16.715f, 20.285f, 16.843f, 20.157f)
                lineTo(21f, 16f)
                curveTo(21.552f, 15.448f, 21.552f, 14.552f, 21f, 14f)
                curveTo(20.448f, 13.448f, 19.552f, 13.448f, 19f, 14f)
                lineTo(14.843f, 18.157f)
                curveTo(14.715f, 18.285f, 14.652f, 18.348f, 14.601f, 18.421f)
                curveTo(14.556f, 18.485f, 14.519f, 18.555f, 14.49f, 18.628f)
                curveTo(14.458f, 18.71f, 14.44f, 18.799f, 14.405f, 18.975f)
                lineTo(14f, 21f)
                close()
            }
        }.build()

        return _Feedback!!
    }

@Suppress("ObjectPropertyName")
private var _Feedback: ImageVector? = null
