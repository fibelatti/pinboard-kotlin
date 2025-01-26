package com.fibelatti.ui.theme

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
internal val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0051E1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE1FF),
    onPrimaryContainer = Color(0xFF00164D),

    secondary = Color(0xFF2157C8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDBE1FF),
    onSecondaryContainer = Color(0xFF00184A),

    tertiary = Color(0xFF255EA6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5E3FF),
    onTertiaryContainer = Color(0xFF001B3C),

    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFEFBFF),
    onBackground = Color(0xFF1B1B1F),

    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE2E1EC),
    onSurfaceVariant = Color(0xFF45464F),

    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC6C6D0),

    inverseSurface = Color(0xFF303034),
    inverseOnSurface = Color(0xFFF2F0F4),
    inversePrimary = Color(0xFFB5C4FF),
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB5C4FF),
    onPrimary = Color(0xFF00297B),
    primaryContainer = Color(0xFF003CAC),
    onPrimaryContainer = Color(0xFFDCE1FF),

    secondary = Color(0xFFB3C5FF),
    onSecondary = Color(0xFF002A76),
    secondaryContainer = Color(0xFF003FA5),
    onSecondaryContainer = Color(0xFFDBE1FF),

    tertiary = Color(0xFFA8C8FF),
    onTertiary = Color(0xFF003061),
    tertiaryContainer = Color(0xFF004689),
    onTertiaryContainer = Color(0xFFD5E3FF),

    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE4E2E6),

    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE4E2E6),
    surfaceVariant = Color(0xFF45464F),
    onSurfaceVariant = Color(0xFFC6C6D0),

    outline = Color(0xFF8F909A),
    outlineVariant = Color(0xFF45464F),

    inverseSurface = Color(0xFFE4E2E6),
    inverseOnSurface = Color(0xFF1B1B1F),
    inversePrimary = Color(0xFF0051E1),
)
// endregion Material Colors
