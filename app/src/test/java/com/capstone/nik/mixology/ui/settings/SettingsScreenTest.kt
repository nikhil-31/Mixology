package com.capstone.nik.mixology.ui.settings

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.ChatCredits
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import com.capstone.nik.mixology.ui.theme.ThemeMode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsAppearanceOptions_andReportsSelection() {
        val selected = mutableListOf<ThemeMode>()
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = { selected.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("APPEARANCE").assertIsDisplayed()
        composeRule.onNodeWithText("System default").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_theme_dark").performClick()
        composeRule.onNodeWithTag("settings_theme_light").performClick()
        composeRule.onNodeWithText("Shopping list").assertIsDisplayed()

        assertEquals(listOf(ThemeMode.DARK, ThemeMode.LIGHT), selected)
    }

    @Test
    fun chatCredits_showWhatIsLeftAndWhenTheyReset() {
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = {},
                    access = Access.Trial(3),
                    // 2026-11-01T00:00:00Z
                    chatCredits = ChatCredits(488, 500, java.util.Date(1_793_491_200_000L)),
                )
            }
        }

        composeRule.onNodeWithTag("settings_chat_credits").assertIsDisplayed()
        composeRule.onNodeWithText("488 of 500 Ada credits left").assertIsDisplayed()
        composeRule.onNodeWithText("Resets Nov 1").assertIsDisplayed()
    }

    @Test
    fun chatCredits_hiddenWithoutPremiumOrCredits() {
        var access by mutableStateOf<Access>(Access.Free)
        var credits by mutableStateOf<ChatCredits?>(ChatCredits(488, 500, null))
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = {},
                    access = access,
                    chatCredits = credits,
                )
            }
        }
        composeRule.onNodeWithTag("settings_chat_credits").assertDoesNotExist()

        access = Access.Subscribed(null, null)
        credits = null
        composeRule.onNodeWithTag("settings_chat_credits").assertDoesNotExist()
    }

    @Test
    fun shoppingList_reportsClicks() {
        var shopping = 0
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = {},
                    onShoppingList = { shopping += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("settings_shopping_list").performClick()
        assertEquals(1, shopping)
    }

    @Test
    fun playRows_reportClicks() {
        var updates = 0
        var rate = 0
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = {},
                    onCheckForUpdates = { updates += 1 },
                    onRateApp = { rate += 1 },
                )
            }
        }

        composeRule.onNodeWithText("GOOGLE PLAY").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_check_for_updates").performClick()
        composeRule.onNodeWithTag("settings_rate_app").performClick()
        assertEquals(1, updates)
        assertEquals(1, rate)
    }

    @Test
    fun importRecipeRow_reportsClicks() {
        var imports = 0
        composeRule.setContent {
            MixologyTheme {
                SettingsScreen(
                    themeMode = ThemeMode.SYSTEM,
                    onThemeModeSelected = {},
                    onImportRecipe = { imports += 1 },
                )
            }
        }

        composeRule.onNodeWithTag("settings_import_recipe").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Import recipe").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_import_recipe").performClick()
        assertEquals(1, imports)
    }
}
