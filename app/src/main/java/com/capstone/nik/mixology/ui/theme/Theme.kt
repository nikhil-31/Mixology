package com.capstone.nik.mixology.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MixologyLightColorScheme = lightColorScheme(
    primary = MixologyRed,
    onPrimary = Color.White,
    primaryContainer = MixologyGray,
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
    onError = Color.White,
)

private val MixologyDarkColorScheme = darkColorScheme(
    primary = MixologyRedLight,
    onPrimary = Color.White,
    primaryContainer = MixologyDarkSurfaceVariant,
    onPrimaryContainer = MixologyOnDark,
    secondary = MixologyNavyLight,
    onSecondary = MixologyNavy,
    background = MixologyDarkBackground,
    onBackground = MixologyOnDark,
    surface = MixologyDarkSurface,
    onSurface = MixologyOnDark,
    surfaceVariant = MixologyDarkSurfaceVariant,
    onSurfaceVariant = MixologyOnDarkVariant,
    outline = MixologyDarkOutline,
    error = MixologyRedLight,
    onError = Color.White,
)

@Composable
fun MixologyTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val themeMode = rememberThemeMode()
    val useDark = darkTheme ?: when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (useDark) MixologyDarkColorScheme else MixologyLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDark
                isAppearanceLightNavigationBars = !useDark
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MixologyTypography,
        content = content,
    )
}
