package com.vahab.caryar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentTeal,
    onPrimary = Color.White,
    primaryContainer = AccentMuted,
    onPrimaryContainer = TextPrimary,
    secondary = AccentTealLight,
    onSecondary = Color.Black,
    background = GraphiteBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun CarYarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
