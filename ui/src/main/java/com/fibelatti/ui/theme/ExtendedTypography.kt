package com.fibelatti.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fibelatti.ui.R

@Immutable
data class ExtendedTypography(
    val title: TextStyle,
    val sectionTitle: TextStyle,
    val body: TextStyle,
    val detail: TextStyle,
    val caveat: TextStyle,
    val tag: TextStyle,
    val listItem: TextStyle,
)

private val openSansRegular = FontFamily(Font(R.font.opensansregular))

internal val extendedTypography = ExtendedTypography(
    title = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    sectionTitle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    body = TextStyle(
        fontSize = 16.sp,
        fontFamily = openSansRegular,
    ),
    detail = TextStyle(
        fontSize = 14.sp,
        fontFamily = openSansRegular,
    ),
    caveat = TextStyle(
        fontSize = 12.sp,
        fontFamily = openSansRegular,
    ),
    tag = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    listItem = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
)

internal val LocalExtendedTypography = staticCompositionLocalOf {
    extendedTypography
}
