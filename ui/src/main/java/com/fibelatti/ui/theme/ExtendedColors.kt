package com.fibelatti.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// region Extended Colors
@Immutable
public data class ExtendedColors(
    val backgroundNoOverlay: Color,
)

internal val ExtendedLightColorScheme = ExtendedColors(
    backgroundNoOverlay = Color(0xFFFFFFFF),
)

internal val ExtendedDarkColorScheme = ExtendedColors(
    backgroundNoOverlay = Color(0xFF000000),
)

internal val LocalExtendedColors = staticCompositionLocalOf { ExtendedLightColorScheme }
// endregion Extended Colors

// region Material Colors
internal val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF3F5F90),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF254777),
    inversePrimary = Color(0xFFA8C8FF),

    secondary = Color(0xFF555F71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E3F8),
    onSecondaryContainer = Color(0xFF3E4758),

    tertiary = Color(0xFF595992),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE2DFFF),
    onTertiaryContainer = Color(0xFF414178),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),

    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C20),

    surface = Color(0xFFF9F9FF),
    surfaceDim = Color(0xFFD9D9E0),
    surfaceBright = Color(0xFFF9F9FF),
    surfaceVariant = Color(0xFFE0E2EC),
    surfaceTint = Color(0xFF3F5F90),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E8EE),
    surfaceContainerHighest = Color(0xFFE1E2E9),

    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF43474E),

    inverseSurface = Color(0xFF2E3035),
    inverseOnSurface = Color(0xFFF0F0F7),

    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6CF),

    scrim = Color(0xFF000000),

    primaryFixed = Color(0xFFD6E3FF),
    onPrimaryFixed = Color(0xFF001B3C),
    primaryFixedDim = Color(0xFFA8C8FF),
    onPrimaryFixedVariant = Color(0xFF254777),

    secondaryFixed = Color(0xFFD9E3F8),
    onSecondaryFixed = Color(0xFF121C2B),
    secondaryFixedDim = Color(0xFFBDC7DC),
    onSecondaryFixedVariant = Color(0xFF3E4758),

    tertiaryFixed = Color(0xFFE2DFFF),
    onTertiaryFixed = Color(0xFF14134A),
    tertiaryFixedDim = Color(0xFFC2C1FF),
    onTertiaryFixedVariant = Color(0xFF414178),
)

internal val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF06305F),
    primaryContainer = Color(0xFF254777),
    onPrimaryContainer = Color(0xFFD6E3FF),
    inversePrimary = Color(0xFF3F5F90),

    secondary = Color(0xFFBDC7DC),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3E4758),
    onSecondaryContainer = Color(0xFFD9E3F8),

    tertiary = Color(0xFFC2C1FF),
    onTertiary = Color(0xFF2A2A60),
    tertiaryContainer = Color(0xFF414178),
    onTertiaryContainer = Color(0xFFE2DFFF),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF111318),
    onBackground = Color(0xFFE1E2E9),

    surface = Color(0xFF111318),
    surfaceDim = Color(0xFF111318),
    surfaceBright = Color(0xFF37393E),
    surfaceVariant = Color(0xFF43474E),
    surfaceTint = Color(0xFFA8C8FF),

    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),

    onSurface = Color(0xFFE1E2E9),
    onSurfaceVariant = Color(0xFFC4C6CF),

    inverseSurface = Color(0xFFE1E2E9),
    inverseOnSurface = Color(0xFF2E3035),

    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF43474E),

    scrim = Color(0xFF000000),

    primaryFixed = Color(0xFFD6E3FF),
    onPrimaryFixed = Color(0xFF001B3C),
    primaryFixedDim = Color(0xFFA8C8FF),
    onPrimaryFixedVariant = Color(0xFF254777),

    secondaryFixed = Color(0xFFD9E3F8),
    onSecondaryFixed = Color(0xFF121C2B),
    secondaryFixedDim = Color(0xFFBDC7DC),
    onSecondaryFixedVariant = Color(0xFF3E4758),

    tertiaryFixed = Color(0xFFE2DFFF),
    onTertiaryFixed = Color(0xFF14134A),
    tertiaryFixedDim = Color(0xFFC2C1FF),
    onTertiaryFixedVariant = Color(0xFF414178),
)
// endregion Material Colors
