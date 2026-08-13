package com.capstone.nik.mixology.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MixologyColorScheme = lightColorScheme(
    primary = Color.White,
    onPrimary = MixologyText,
    primaryContainer = Color.White,
    onPrimaryContainer = MixologyText,
    secondary = MixologyNavy,
    onSecondary = Color.White,
    background = MixologyGray,
    onBackground = MixologyText,
    surface = Color.White,
    onSurface = MixologyText,
    surfaceVariant = MixologyGray,
    onSurfaceVariant = MixologyText,
    outline = MixologyHighlight,
    error = MixologyRed,
)

@Composable
fun MixologyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MixologyColorScheme,
        typography = MixologyTypography,
        content = content,
    )
}
