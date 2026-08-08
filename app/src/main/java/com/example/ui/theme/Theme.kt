package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IceBlue,
    onPrimary = DeepNavy,
    primaryContainer = DeepNavy,
    onPrimaryContainer = IceBlueDim,
    secondary = AccentBlue,
    onSecondary = SurfaceLowest,
    background = DarkNavy,
    onBackground = SurfaceLowest,
    surface = DarkNavy,
    onSurface = SurfaceLowest,
    surfaceVariant = MutedNavy,
    onSurfaceVariant = SurfaceHigh,
    outline = OutlineGrey
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    onPrimary = SurfaceLowest,
    primaryContainer = Color(0xFF1A2B3C),
    onPrimaryContainer = IceBlueDim,
    secondary = RoyalBlue,
    onSecondary = SurfaceLowest,
    secondaryContainer = AccentBlue,
    onSecondaryContainer = Color(0xFFFEFCFF),
    background = LightSlateBackground,
    onBackground = DarkNavy,
    surface = LightSlateBackground,
    onSurface = DarkNavy,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = MutedSlate,
    outline = OutlineGrey,
    outlineVariant = OutlineVariantGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
