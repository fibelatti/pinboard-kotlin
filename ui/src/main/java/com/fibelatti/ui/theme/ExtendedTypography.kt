package com.fibelatti.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fibelatti.ui.R

private val openSansRegular = FontFamily(Font(R.font.opensansregular))

internal val ExtendedTypography = Typography(
    titleLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    titleMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    titleSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = openSansRegular,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontFamily = openSansRegular,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontFamily = openSansRegular,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontFamily = openSansRegular,
    ),
)
