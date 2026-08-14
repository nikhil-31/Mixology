package com.capstone.nik.mixology.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.theme.ThemeMode
import com.capstone.nik.mixology.ui.theme.ThemePreferences
import com.capstone.nik.mixology.ui.theme.rememberThemeMode

@Composable
fun SettingsRoute() {
    val context = LocalContext.current
    val themeMode = rememberThemeMode()
    SettingsScreen(
        themeMode = themeMode,
        onThemeModeSelected = { ThemePreferences.set(context, it) },
    )
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_appearance),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onThemeModeSelected(mode) }
                    .padding(vertical = 4.dp)
                    .testTag("settings_theme_${mode.name.lowercase()}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = themeMode == mode,
                    onClick = null,
                )
                Text(
                    text = stringResource(mode.titleRes),
                    color = colors.onBackground,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private val ThemeMode.titleRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
