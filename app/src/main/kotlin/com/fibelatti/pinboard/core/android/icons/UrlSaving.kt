package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.UrlSaving: ImageVector
    get() {
        if (_UrlSaving != null) {
            return _UrlSaving!!
        }
        _UrlSaving = ImageVector.Builder(
            name = "UrlSaving",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(30.5f, 30.5f)
                curveToRelative(-0.148f, 0.148f, -0.344f, 0.224f, -0.54f, 0.224f)
                curveToRelative(-0.176f, 0f, -0.353f, -0.061f, -0.497f, -0.184f)
                lineToRelative(-11.084f, -9.505f)
                lineToRelative(2.659f, -2.659f)
                lineToRelative(9.501f, 11.087f)
                curveTo(30.799f, 29.766f, 30.782f, 30.218f, 30.5f, 30.5f)
                close()
                moveTo(24.293f, 12.293f)
                lineToRelative(-0.879f, -0.879f)
                curveToRelative(-0.391f, -0.391f, -0.902f, -0.586f, -1.414f, -0.586f)
                reflectiveCurveToRelative(-1.024f, 0.195f, -1.414f, 0.586f)
                lineTo(20.45f, 11.55f)
                lineToRelative(-8.807f, -7.206f)
                curveToRelative(0.717f, -0.785f, 0.702f, -1.999f, -0.057f, -2.758f)
                lineToRelative(-0.879f, -0.879f)
                curveToRelative(-0.391f, -0.391f, -1.024f, -0.391f, -1.414f, 0f)
                lineTo(0.707f, 9.293f)
                curveToRelative(-0.391f, 0.391f, -0.391f, 1.024f, 0f, 1.414f)
                lineToRelative(0.879f, 0.879f)
                curveToRelative(0.389f, 0.389f, 0.897f, 0.582f, 1.406f, 0.582f)
                curveToRelative(0.485f, 0f, 0.97f, -0.176f, 1.352f, -0.525f)
                lineToRelative(7.206f, 8.807f)
                lineToRelative(-0.136f, 0.136f)
                curveToRelative(-0.781f, 0.781f, -0.781f, 2.047f, 0f, 2.828f)
                lineToRelative(0.879f, 0.879f)
                curveToRelative(0.391f, 0.391f, 1.024f, 0.391f, 1.414f, 0f)
                lineToRelative(10.586f, -10.586f)
                curveTo(24.683f, 13.317f, 24.683f, 12.683f, 24.293f, 12.293f)
                close()
            }
        }.build()

        return _UrlSaving!!
    }

@Suppress("ObjectPropertyName")
private var _UrlSaving: ImageVector? = null
