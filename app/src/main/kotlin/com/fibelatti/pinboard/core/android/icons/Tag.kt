package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Tag: ImageVector
    get() {
        if (_Tag != null) {
            return _Tag!!
        }
        _Tag = ImageVector.Builder(
            name = "Tag",
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
                moveTo(7.05f, 7.05f)
                horizontalLineTo(7.06f)
                moveTo(10.512f, 3f)
                horizontalLineTo(7.8f)
                curveTo(6.12f, 3f, 5.28f, 3f, 4.638f, 3.327f)
                curveTo(4.074f, 3.615f, 3.615f, 4.074f, 3.327f, 4.638f)
                curveTo(3f, 5.28f, 3f, 6.12f, 3f, 7.8f)
                verticalLineTo(10.512f)
                curveTo(3f, 11.245f, 3f, 11.612f, 3.083f, 11.958f)
                curveTo(3.156f, 12.264f, 3.278f, 12.556f, 3.442f, 12.825f)
                curveTo(3.628f, 13.128f, 3.887f, 13.387f, 4.406f, 13.906f)
                lineTo(9.106f, 18.606f)
                curveTo(10.294f, 19.794f, 10.888f, 20.388f, 11.573f, 20.611f)
                curveTo(12.175f, 20.806f, 12.825f, 20.806f, 13.427f, 20.611f)
                curveTo(14.112f, 20.388f, 14.706f, 19.794f, 15.894f, 18.606f)
                lineTo(18.606f, 15.894f)
                curveTo(19.794f, 14.706f, 20.388f, 14.112f, 20.611f, 13.427f)
                curveTo(20.806f, 12.825f, 20.806f, 12.175f, 20.611f, 11.573f)
                curveTo(20.388f, 10.888f, 19.794f, 10.294f, 18.606f, 9.106f)
                lineTo(13.906f, 4.406f)
                curveTo(13.387f, 3.887f, 13.128f, 3.628f, 12.825f, 3.442f)
                curveTo(12.556f, 3.278f, 12.264f, 3.156f, 11.958f, 3.083f)
                curveTo(11.612f, 3f, 11.245f, 3f, 10.512f, 3f)
                close()
                moveTo(7.55f, 7.05f)
                curveTo(7.55f, 7.326f, 7.326f, 7.55f, 7.05f, 7.55f)
                curveTo(6.774f, 7.55f, 6.55f, 7.326f, 6.55f, 7.05f)
                curveTo(6.55f, 6.774f, 6.774f, 6.55f, 7.05f, 6.55f)
                curveTo(7.326f, 6.55f, 7.55f, 6.774f, 7.55f, 7.05f)
                close()
            }
        }.build()

        return _Tag!!
    }

@Suppress("ObjectPropertyName")
private var _Tag: ImageVector? = null
