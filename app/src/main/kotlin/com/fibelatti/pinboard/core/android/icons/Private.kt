package com.fibelatti.pinboard.core.android.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Private: ImageVector
    get() {
        if (_Private != null) {
            return _Private!!
        }
        _Private = ImageVector.Builder(
            name = "Private",
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
                moveTo(7f, 10.029f)
                curveTo(7.471f, 10f, 8.053f, 10f, 8.8f, 10f)
                horizontalLineTo(15.2f)
                curveTo(15.947f, 10f, 16.529f, 10f, 17f, 10.029f)
                moveTo(7f, 10.029f)
                curveTo(6.412f, 10.065f, 5.994f, 10.146f, 5.638f, 10.327f)
                curveTo(5.074f, 10.615f, 4.615f, 11.073f, 4.327f, 11.638f)
                curveTo(4f, 12.28f, 4f, 13.12f, 4f, 14.8f)
                verticalLineTo(16.2f)
                curveTo(4f, 17.88f, 4f, 18.72f, 4.327f, 19.362f)
                curveTo(4.615f, 19.927f, 5.074f, 20.385f, 5.638f, 20.673f)
                curveTo(6.28f, 21f, 7.12f, 21f, 8.8f, 21f)
                horizontalLineTo(15.2f)
                curveTo(16.88f, 21f, 17.72f, 21f, 18.362f, 20.673f)
                curveTo(18.927f, 20.385f, 19.385f, 19.927f, 19.673f, 19.362f)
                curveTo(20f, 18.72f, 20f, 17.88f, 20f, 16.2f)
                verticalLineTo(14.8f)
                curveTo(20f, 13.12f, 20f, 12.28f, 19.673f, 11.638f)
                curveTo(19.385f, 11.073f, 18.927f, 10.615f, 18.362f, 10.327f)
                curveTo(18.006f, 10.146f, 17.588f, 10.065f, 17f, 10.029f)
                moveTo(7f, 10.029f)
                verticalLineTo(8f)
                curveTo(7f, 5.239f, 9.239f, 3f, 12f, 3f)
                curveTo(14.761f, 3f, 17f, 5.239f, 17f, 8f)
                verticalLineTo(10.029f)
            }
        }.build()

        return _Private!!
    }

@Suppress("ObjectPropertyName")
private var _Private: ImageVector? = null
