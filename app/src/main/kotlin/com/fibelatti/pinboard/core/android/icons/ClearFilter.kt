package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.ClearFilter: ImageVector
    get() {
        if (_ClearFilter != null) {
            return _ClearFilter!!
        }
        _ClearFilter = ImageVector.Builder(
            name = "ClearFilter",
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
                moveTo(15f, 15f)
                lineTo(21f, 21f)
                moveTo(21f, 15f)
                lineTo(15f, 21f)
                moveTo(10f, 21f)
                verticalLineTo(14.663f)
                curveTo(10f, 14.418f, 10f, 14.296f, 9.972f, 14.181f)
                curveTo(9.948f, 14.079f, 9.907f, 13.981f, 9.853f, 13.892f)
                curveTo(9.791f, 13.791f, 9.704f, 13.704f, 9.531f, 13.531f)
                lineTo(3.469f, 7.469f)
                curveTo(3.296f, 7.296f, 3.209f, 7.209f, 3.147f, 7.108f)
                curveTo(3.093f, 7.019f, 3.052f, 6.921f, 3.028f, 6.819f)
                curveTo(3f, 6.704f, 3f, 6.582f, 3f, 6.337f)
                verticalLineTo(4.6f)
                curveTo(3f, 4.04f, 3f, 3.76f, 3.109f, 3.546f)
                curveTo(3.205f, 3.358f, 3.358f, 3.205f, 3.546f, 3.109f)
                curveTo(3.76f, 3f, 4.04f, 3f, 4.6f, 3f)
                horizontalLineTo(19.4f)
                curveTo(19.96f, 3f, 20.24f, 3f, 20.454f, 3.109f)
                curveTo(20.642f, 3.205f, 20.795f, 3.358f, 20.891f, 3.546f)
                curveTo(21f, 3.76f, 21f, 4.04f, 21f, 4.6f)
                verticalLineTo(6.337f)
                curveTo(21f, 6.582f, 21f, 6.704f, 20.972f, 6.819f)
                curveTo(20.948f, 6.921f, 20.907f, 7.019f, 20.853f, 7.108f)
                curveTo(20.791f, 7.209f, 20.704f, 7.296f, 20.531f, 7.469f)
                lineTo(17f, 11f)
            }
        }.build()

        return _ClearFilter!!
    }

@Suppress("ObjectPropertyName")
private var _ClearFilter: ImageVector? = null
