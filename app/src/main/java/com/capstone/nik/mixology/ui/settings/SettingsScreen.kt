package com.capstone.nik.mixology.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.StarRate
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.ChatCredits
import com.capstone.nik.mixology.ui.paywall.SubscriptionGateViewModel
import com.capstone.nik.mixology.ui.auth.SessionViewModel
import com.capstone.nik.mixology.ui.theme.MixologySectionTitle
import com.capstone.nik.mixology.ui.theme.ThemeMode
import com.capstone.nik.mixology.ui.theme.ThemePreferences
import com.capstone.nik.mixology.ui.theme.rememberThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

const val PRIVACY_POLICY_URL = "https://nikhil-31.github.io/Mixology/privacy-policy"

/** Google Play's page for managing (and cancelling) this app's subscription. */
fun playSubscriptionsUrl(productId: String?, packageName: String): String {
    val sku = productId?.let { "sku=$it&" }.orEmpty()
    return "https://play.google.com/store/account/subscriptions?${sku}package=$packageName"
}

@Composable
fun SettingsRoute(
    onCatalog: () -> Unit = {},
    onShoppingList: () -> Unit = {},
    onMyRecipes: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
    onRateApp: () -> Unit = {},
    privacyOptionsRequired: Boolean = false,
    onPrivacyPolicy: () -> Unit = {},
    onPrivacyOptions: () -> Unit = {},
    sessionViewModel: SessionViewModel = hiltViewModel(),
    subscriptionGate: SubscriptionGateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val themeMode = rememberThemeMode()
    val session by sessionViewModel.session.collectAsStateWithLifecycle()
    val access by subscriptionGate.access.collectAsStateWithLifecycle()
    val chatCredits by subscriptionGate.chatCredits.collectAsStateWithLifecycle()
    SettingsScreen(
        themeMode = themeMode,
        accountEmail = session?.email.orEmpty(),
        onSignOut = sessionViewModel::signOut,
        access = access,
        chatCredits = chatCredits,
        onSubscriptionAction = {
            when (val current = access) {
                is Access.Subscribed -> uriHandler.openUri(
                    playSubscriptionsUrl(current.productId, context.packageName),
                )
                else -> subscriptionGate.openPaywall()
            }
        },
        onThemeModeSelected = { ThemePreferences.set(context, it) },
        onCatalog = onCatalog,
        onShoppingList = onShoppingList,
        onMyRecipes = onMyRecipes,
        onCheckForUpdates = onCheckForUpdates,
        onRateApp = onRateApp,
        privacyOptionsRequired = privacyOptionsRequired,
        onPrivacyPolicy = onPrivacyPolicy,
        onPrivacyOptions = onPrivacyOptions,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    accountEmail: String = "",
    onSignOut: () -> Unit = {},
    access: Access = Access.Unknown,
    chatCredits: ChatCredits? = null,
    onSubscriptionAction: () -> Unit = {},
    onCatalog: () -> Unit = {},
    onShoppingList: () -> Unit = {},
    onMyRecipes: () -> Unit = {},
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
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
    ) {
        SettingsSectionTitle(stringResource(R.string.settings_account))
        ListItem(
            headlineContent = {
                Text(
                    text = accountEmail,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("settings_account_email"),
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = accountEmail.firstOrNull()?.uppercase().orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onPrimaryContainer,
                    )
                }
            },
            trailingContent = {
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("settings_sign_out"),
                ) {
                    Text(stringResource(R.string.settings_sign_out))
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
        if (access !is Access.Unknown) {
            ListItem(
                headlineContent = {
                    Text(
                        text = when (access) {
                            is Access.Subscribed -> stringResource(R.string.settings_subscription_premium)
                            is Access.Trial -> pluralStringResource(
                                R.plurals.settings_trial_days_left,
                                access.daysLeft,
                                access.daysLeft,
                            )
                            else -> stringResource(R.string.settings_subscription_free)
                        },
                        modifier = Modifier.testTag("settings_subscription_status"),
                    )
                },
                trailingContent = {
                    TextButton(
                        onClick = onSubscriptionAction,
                        modifier = Modifier.testTag("settings_subscription_action"),
                    ) {
                        Text(
                            stringResource(
                                if (access is Access.Subscribed) {
                                    R.string.settings_subscription_manage
                                } else {
                                    R.string.settings_subscription_subscribe
                                },
                            ),
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
        // Ada is Premium only, so free accounts have no credits to show.
        if (chatCredits != null && (access is Access.Trial || access is Access.Subscribed)) {
            val resetsOn = chatCredits.resetsAt?.let { rememberMonthDay(it) }
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(
                            R.string.settings_chat_credits_left,
                            chatCredits.remaining,
                            chatCredits.limit,
                        ),
                    )
                },
                supportingContent = resetsOn?.let {
                    { Text(stringResource(R.string.settings_chat_credits_resets, it)) }
                },
                modifier = Modifier.testTag("settings_chat_credits"),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }

        SettingsSectionTitle(stringResource(R.string.settings_appearance))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = themeMode == mode,
                    onClick = { onThemeModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                    modifier = Modifier.testTag("settings_theme_${mode.name.lowercase()}"),
                ) {
                    Text(
                        text = stringResource(mode.titleRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        SettingsSectionTitle(stringResource(R.string.settings_library))
        SettingsRow(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            title = stringResource(R.string.nav_item_browse_catalog),
            onClick = onCatalog,
            tag = "settings_catalog",
        )
        SettingsRow(
            icon = Icons.Outlined.ShoppingCart,
            title = stringResource(R.string.nav_item_shopping_list),
            onClick = onShoppingList,
            tag = "settings_shopping_list",
        )
        SettingsRow(
            icon = Icons.Outlined.EditNote,
            title = stringResource(R.string.my_recipes_title),
            onClick = onMyRecipes,
            tag = "settings_my_recipes",
        )

        SettingsSectionTitle(stringResource(R.string.settings_privacy_header))
        SettingsRow(
            icon = Icons.Outlined.Policy,
            title = stringResource(R.string.settings_privacy),
            onClick = onPrivacyPolicy,
            tag = "settings_privacy_policy",
            chevron = false,
        )
        if (privacyOptionsRequired) {
            SettingsRow(
                icon = Icons.Outlined.PrivacyTip,
                title = stringResource(R.string.settings_privacy_options),
                onClick = onPrivacyOptions,
                tag = "settings_privacy_options",
                chevron = false,
            )
        }

        SettingsSectionTitle(stringResource(R.string.settings_play))
        SettingsRow(
            icon = Icons.Outlined.SystemUpdate,
            title = stringResource(R.string.settings_check_for_updates),
            onClick = onCheckForUpdates,
            tag = "settings_check_for_updates",
            chevron = false,
        )
        SettingsRow(
            icon = Icons.Outlined.StarRate,
            title = stringResource(R.string.settings_rate_app),
            onClick = onRateApp,
            tag = "settings_rate_app",
            chevron = false,
        )

        Text(
            text = stringResource(R.string.settings_credits),
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
        style = MixologySectionTitle,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tag: String,
    chevron: Boolean = true,
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = if (chevron) {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            null
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag),
    )
}

private val ThemeMode.titleRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.settings_theme_system
        ThemeMode.LIGHT -> R.string.settings_theme_light
        ThemeMode.DARK -> R.string.settings_theme_dark
    }

/** "Nov 1" in the user's locale. Credits reset at midnight UTC, so the day is read in UTC. */
@Composable
private fun rememberMonthDay(date: Date): String {
    val locale = Locale.getDefault()
    return remember(date, locale) {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd")
        SimpleDateFormat(pattern, locale).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(date)
    }
}
