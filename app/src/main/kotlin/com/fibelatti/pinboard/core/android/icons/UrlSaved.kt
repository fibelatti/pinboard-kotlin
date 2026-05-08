package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.UrlSaved: ImageVector
    get() {
        if (_UrlSaved != null) {
            return _UrlSaved!!
        }
        _UrlSaved = ImageVector.Builder(
            name = "UrlSaved",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 16.2f)
                lineToRelative(-3.5f, -3.5f)
                curveToRelative(-0.39f, -0.39f, -1.01f, -0.39f, -1.4f, 0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.01f, 0f, 1.4f)
                lineToRelative(4.19f, 4.19f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
                lineTo(20.3f, 7.7f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.01f, 0f, -1.4f)
                curveToRelative(-0.39f, -0.39f, -1.01f, -0.39f, -1.4f, 0f)
                lineTo(9f, 16.2f)
                close()
            }
        }.build()

        return _UrlSaved!!
    }

@Suppress("ObjectPropertyName")
private var _UrlSaved: ImageVector? = null
