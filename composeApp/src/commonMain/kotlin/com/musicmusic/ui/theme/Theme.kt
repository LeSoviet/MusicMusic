package com.musicmusic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LavenderLight,
    onPrimary = TextPrimaryLight,
    primaryContainer = LavenderLight.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimaryLight,
    
    secondary = MintLight,
    onSecondary = TextPrimaryLight,
    secondaryContainer = MintLight.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimaryLight,
    
    tertiary = AccentBlue,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = AccentBlue.copy(alpha = 0.2f),
    onTertiaryContainer = TextPrimaryLight,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = BackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    
    error = ErrorLight,
    onError = SurfaceLight,
    errorContainer = ErrorLight.copy(alpha = 0.1f),
    onErrorContainer = ErrorLight,
    
    outline = TextSecondaryLight.copy(alpha = 0.3f),
    outlineVariant = TextSecondaryLight.copy(alpha = 0.1f),
    
    scrim = TextPrimaryLight.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = LavenderDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = LavenderDark.copy(alpha = 0.2f),
    onPrimaryContainer = TextPrimaryDark,
    
    secondary = MintDark,
    onSecondary = TextPrimaryDark,
    secondaryContainer = MintDark.copy(alpha = 0.2f),
    onSecondaryContainer = TextPrimaryDark,
    
    tertiary = AccentBlue,
    onTertiary = TextPrimaryDark,
    tertiaryContainer = AccentBlue.copy(alpha = 0.2f),
    onTertiaryContainer = TextPrimaryDark,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorDark,
    onError = SurfaceDark,
    errorContainer = ErrorDark.copy(alpha = 0.1f),
    onErrorContainer = ErrorDark,
    
    outline = TextSecondaryDark.copy(alpha = 0.3f),
    outlineVariant = TextSecondaryDark.copy(alpha = 0.1f),
    
    scrim = TextPrimaryDark.copy(alpha = 0.5f)
)

@Composable
fun MusicMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MusicMusicTypography,
        shapes = MusicMusicShapes,
        content = content
    )
}
