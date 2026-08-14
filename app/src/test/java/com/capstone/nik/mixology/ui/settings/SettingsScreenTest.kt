package com.capstone.nik.mixology.ui.settings

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import com.capstone.nik.mixology.ui.theme.ThemeMode
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

        composeRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeRule.onNodeWithText("System default").assertIsDisplayed()
        composeRule.onNodeWithText("Light").assertIsDisplayed()
        composeRule.onNodeWithText("Dark").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_theme_dark").performClick()
        composeRule.onNodeWithTag("settings_theme_light").performClick()

        assertEquals(listOf(ThemeMode.DARK, ThemeMode.LIGHT), selected)
    }
}
