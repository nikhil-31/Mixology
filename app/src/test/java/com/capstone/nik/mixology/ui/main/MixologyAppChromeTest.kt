package com.capstone.nik.mixology.ui.main

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.cash.turbine.test
import com.capstone.nik.mixology.Network.ConnectionError
import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import kotlinx.coroutines.test.runTest
import androidx.compose.ui.layout.positionInRoot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun rememberTabDestination_followsTheBackStackAndKeepsTabUnderOverlays() {
        var route by mutableStateOf<String?>(RECIPES_HOME_ROUTE)
        var destination: DrawerDestination? = null
        composeRule.setContent {
            destination = rememberTabDestination(route, null)
        }
        composeRule.waitForIdle()
        assertEquals(DrawerDestination.RecipesHome, destination)

        route = BAR_ROUTE
        composeRule.waitForIdle()
        assertEquals(DrawerDestination.Bar, destination)

        // Details on top of My Bar: the bar stays the tab underneath.
        route = DETAILS_ROUTE
        composeRule.waitForIdle()
        assertEquals(DrawerDestination.Bar, destination)

        // System Back to the home tab: the title and tab follow.
        route = RECIPES_HOME_ROUTE
        composeRule.waitForIdle()
        assertEquals(DrawerDestination.RecipesHome, destination)
    }

    @Test
    fun screenHeader_showsTitleAndReportsSearch() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                ScreenHeader(
                    title = "Home",
                    showUp = false,
                    showSearch = true,
                    onUp = { events.add("up") },
                    onSearch = { events.add("search") },
                    onSettings = { events.add("settings") },
                )
            }
        }

        composeRule.onNodeWithTag("screen_title").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Up navigation").assertDoesNotExist()
        composeRule.onNodeWithTag("header_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("header_history").assertDoesNotExist()
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
                    onUp = { up = true },
                    onSearch = {},
                    onSettings = {},
                )
            }
        }

        composeRule.onNodeWithText("Saved Cocktails").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Search").assertDoesNotExist()
        composeRule.onNodeWithTag("header_settings").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        assertTrue(up)
    }

    @Test
    fun screenHeader_showsHistoryNextToSettings_onlyWhenGiven() {
        val events = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                ScreenHeader(
                    title = "Shopping list",
                    showUp = false,
                    showSearch = false,
                    onUp = {},
                    onSearch = {},
                    onSettings = { events.add("settings") },
                    onHistory = { events.add("history") },
                )
            }
        }

        val history = composeRule.onNodeWithTag("header_history").fetchSemanticsNode().positionInRoot
        val settings = composeRule.onNodeWithTag("header_settings").fetchSemanticsNode().positionInRoot
        // Right beside Settings, on the same row.
        assertTrue(history.x < settings.x)
        assertEquals(history.y, settings.y)
        composeRule.onNodeWithContentDescription("Shopping history").performClick()
        assertEquals(listOf("history"), events)
    }

    @Test
    fun offlineBanner_retry_invokesCallback() {
        var retries = 0
        composeRule.setContent {
            MixologyTheme {
                OfflineBanner(error = ConnectionError.NO_CONNECTION, onRetry = { retries += 1 })
            }
        }

        composeRule.onNodeWithTag("offline_banner").assertIsDisplayed()
        composeRule.onNodeWithText("No Internet Connection").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun offlineBanner_serverUnreachable_retryEmitsRetries() = runTest {
        val monitor = NetworkMonitor.forTests(online = true)
        monitor.markUnreachable()
        composeRule.setContent {
            MixologyTheme {
                val online by monitor.online.collectAsState()
                val reachable by monitor.serverReachable.collectAsState()
                connectionBannerError(online, reachable)?.let { error ->
                    OfflineBanner(error = error, onRetry = monitor::retry)
                }
            }
        }

        composeRule.onNodeWithTag("offline_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Can’t reach Mixology").assertIsDisplayed()
        monitor.retries.test {
            composeRule.onNodeWithText("Retry").performClick()
            awaitItem()
        }

        monitor.markReachable()
        composeRule.onNodeWithTag("offline_banner").assertDoesNotExist()
    }

    @Test
    fun connectionBannerError_prefersOfflineOverUnreachable() {
        assertEquals(ConnectionError.NO_CONNECTION, connectionBannerError(online = false, serverReachable = false))
        assertEquals(ConnectionError.SERVER_UNREACHABLE, connectionBannerError(online = true, serverReachable = false))
        assertNull(connectionBannerError(online = true, serverReachable = true))
    }
}
