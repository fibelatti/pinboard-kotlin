package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.UrlSavedError: ImageVector
    get() {
        if (_UrlSavedError != null) {
            return _UrlSavedError!!
        }
        _UrlSavedError = ImageVector.Builder(
            name = "UrlSavedError",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.3f, 5.71f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
                lineTo(12f, 10.59f)
                lineTo(7.11f, 5.7f)
                curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                lineTo(10.59f, 12f)
                lineTo(5.7f, 16.89f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
                lineTo(12f, 13.41f)
                lineToRelative(4.89f, 4.89f)
                curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f)
                lineTo(13.41f, 12f)
                lineToRelative(4.89f, -4.89f)
                curveToRelative(0.38f, -0.38f, 0.38f, -1.02f, 0f, -1.4f)
                close()
            }
        }.build()

        return _UrlSavedError!!
    }

@Suppress("ObjectPropertyName")
private var _UrlSavedError: ImageVector? = null
