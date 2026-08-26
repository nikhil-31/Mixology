package com.capstone.nik.mixology.ui.main

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class MixologyAppChromeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun screenHeader_showsTitleAndReportsSearchSettings() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                ScreenHeader(
                    title = "Home",
                    showUp = false,
                    showSearch = true,
                    showSettings = true,
                    onUp = { events.add("up") },
                    onSearch = { events.add("search") },
                    onSettings = { events.add("settings") },
                )
            }
        }

        composeRule.onNodeWithTag("screen_title").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Up navigation").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Search").performClick()
        composeRule.onNodeWithContentDescription("Settings").performClick()
        assertEquals(listOf("search", "settings"), events)
    }

    @Test
    fun screenHeader_upNavigation_invokesCallback() {
        var up = false
        composeRule.setContent {
            MixologyTheme {
                ScreenHeader(
                    title = "Saved Cocktails",
                    showUp = true,
                    showSearch = false,
                    showSettings = false,
                    onUp = { up = true },
                    onSearch = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("Saved Cocktails").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Settings").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        assertTrue(up)
    }

    @Test
    fun offlineBanner_retry_invokesCallback() {
        var retries = 0
        composeRule.setContent {
            MixologyTheme {
                OfflineBanner(onRetry = { retries += 1 })
            }
        }

        composeRule.onNodeWithTag("offline_banner").assertIsDisplayed()
        composeRule.onNodeWithText("No Internet Connection").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }
}
