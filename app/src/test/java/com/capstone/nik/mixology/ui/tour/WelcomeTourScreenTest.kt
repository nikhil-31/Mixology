package com.capstone.nik.mixology.ui.tour

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.ui.theme.MixologyTheme
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
class WelcomeTourScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var done = 0

    private fun show() {
        composeRule.setContent {
            MixologyTheme {
                WelcomeTourScreen(onDone = { done += 1 })
            }
        }
    }

    @Test
    fun next_pagesForwardThroughEveryPage_andGetStartedFinishes() {
        show()

        composeRule.onNodeWithTag("tour_screen").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to Mixology").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_done").assertDoesNotExist()

        listOf("Home", "My Bar", "Shopping", "Ada").forEach { title ->
            composeRule.onNodeWithTag("tour_next").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithText(title).assertIsDisplayed()
        }
        assertEquals(0, done)

        // The last page has nothing to skip and nowhere further to go.
        composeRule.onNodeWithTag("tour_next").assertDoesNotExist()
        composeRule.onNodeWithTag("tour_skip").assertDoesNotExist()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_done").performClick()
        assertEquals(1, done)
    }

    @Test
    fun skip_finishesFromTheFirstPage() {
        show()

        composeRule.onNodeWithText("Skip").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_skip").performClick()
        assertEquals(1, done)
    }

    @Test
    fun skip_isStillThereMidway() {
        show()

        composeRule.onNodeWithTag("tour_next").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tour_skip").assertIsDisplayed().performClick()
        assertEquals(1, done)
    }

    @Test
    @Config(qualifiers = "w320dp-h480dp-xhdpi", fontScale = 2f)
    fun smallScreenAndLargeText_keepTheControlsOnScreen() {
        show()

        composeRule.onNodeWithTag("tour_skip").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_next").assertIsDisplayed()
    }
}
