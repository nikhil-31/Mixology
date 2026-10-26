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
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.auth.SessionViewModel
import com.capstone.nik.mixology.ui.theme.ThemeMode
import com.capstone.nik.mixology.ui.theme.ThemePreferences
import com.capstone.nik.mixology.ui.theme.rememberThemeMode

const val PRIVACY_POLICY_URL = "https://nikhil-31.github.io/Mixology/privacy-policy"

@Composable
fun SettingsRoute(
    onCatalog: () -> Unit = {},
    onShoppingList: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onRateApp: () -> Unit = {},
    privacyOptionsRequired: Boolean = false,
    onPrivacyPolicy: () -> Unit = {},
    onPrivacyOptions: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val themeMode = rememberThemeMode()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    SettingsScreen(
        themeMode = themeMode,
        accountEmail = session?.email.orEmpty(),
        onSignOut = sessionViewModel::signOut,
        onThemeModeSelected = { ThemePreferences.set(context, it) },
        onCatalog = onCatalog,
        onShoppingList = onShoppingList,
        onCheckForUpdates = onCheckForUpdates,
        onRateApp = onRateApp,
        privacyOptionsRequired = privacyOptionsRequired,
        onPrivacyPolicy = onPrivacyPolicy,
        onPrivacyOptions = onPrivacyOptions,
    )
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    accountEmail: String = "",
    onSignOut: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onShoppingList: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onRateApp: () -> Unit = {},
    privacyOptionsRequired: Boolean = false,
    onPrivacyPolicy: () -> Unit = {},
    onPrivacyOptions: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_account),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = accountEmail,
                color = colors.onSurfaceVariant,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
                    .testTag("settings_account_email"),
            )
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("settings_sign_out"),
            ) {
                Text(stringResource(R.string.settings_sign_out))
            }
        }
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
        Text(
            text = stringResource(R.string.nav_item_browse_catalog),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCatalog)
                .padding(top = 24.dp, bottom = 12.dp)
                .testTag("settings_catalog"),
        )
        Text(
            text = stringResource(R.string.nav_item_shopping_list),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onShoppingList)
                .padding(vertical = 12.dp)
                .testTag("settings_shopping_list"),
        )
        Text(
            text = stringResource(R.string.settings_privacy),
            color = colors.onBackground,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPrivacyPolicy)
                .padding(vertical = 12.dp)
                .testTag("settings_privacy_policy"),
        )
        if (privacyOptionsRequired) {
            Text(
                text = stringResource(R.string.settings_privacy_options),
                color = colors.onBackground,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPrivacyOptions)
                    .padding(vertical = 12.dp)
                    .testTag("settings_privacy_options"),
            )
        }
        Text(
            text = stringResource(R.string.settings_play),
            color = colors.onBackground,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        Text(
            text = stringResource(R.string.settings_check_for_updates),
            color = colors.onBackground,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCheckForUpdates)
                .padding(vertical = 12.dp)
                .testTag("settings_check_for_updates"),
        )
        Text(
            text = stringResource(R.string.settings_rate_app),
            color = colors.onBackground,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onRateApp)
                .padding(vertical = 12.dp)
                .testTag("settings_rate_app"),
        )
        Text(
            text = stringResource(R.string.settings_credits),
            color = colors.onBackground.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 24.dp),
        )
    }
}

private val ThemeMode.titleRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }
