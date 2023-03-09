@file:Suppress("MagicNumber")

package com.fibelatti.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// region Extended Colors
@Immutable
data class ExtendedColors(
    val statusBar: Color,
    val navigationBar: Color,
    val icon: Color,
)

internal val ExtendedLightColorScheme = ExtendedColors(
    statusBar = Color(0xFFC2C2C2),
    navigationBar = Color(0xFF9E9E9E),
    icon = Color(0xFF424242),
)

internal val ExtendedDarkColorScheme = ExtendedColors(
    statusBar = Color(0x00000000),
    navigationBar = Color(0xFF000000),
    icon = Color(0xFFF5F5F5),
)

internal val LocalExtendedColors = staticCompositionLocalOf { ExtendedLightColorScheme }
// endregion Extended Colors

// region Material Colors
internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0151E1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDAE1FF),
    onPrimaryContainer = Color(0xFF001550),

    secondary = Color(0xFF114AEF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE1FF),
    onSecondaryContainer = Color(0xFF001158),

    tertiary = Color(0xFF006781),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB5EAFF),
    onTertiaryContainer = Color(0xFF001F29),

    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410E0B),

    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFF79747E),

    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = Color(0xFFB4C5FF),
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB4C5FF),
    onPrimary = Color(0xFF00277E),
    primaryContainer = Color(0xFF003BB1),
    onPrimaryContainer = Color(0xFFDAE1FF),

    secondary = Color(0xFFB7C4FF),
    onSecondary = Color(0xFF00228C),
    secondaryContainer = Color(0xFF0033C2),
    onSecondaryContainer = Color(0xFFDCE1FF),

    tertiary = Color(0xFF4AD6FF),
    onTertiary = Color(0xFF003544),
    tertiaryContainer = Color(0xFF004D61),
    onTertiaryContainer = Color(0xFFB5EAFF),

    error = Color(0xFFF2B8B5),
    errorContainer = Color(0xFF8C1D18),
    onError = Color(0xFF601410),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color(0xFF000000),
    onBackground = Color(0xFFE6E1E5),

    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF938F99),

    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = Color(0xFF0151E1),
)
// endregion Material Colors
