package com.capstone.nik.mixology

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.capstone.nik.mixology.Activities.ActivityMain
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.putDrinkExtra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ActivityMainEspressoTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun launchWithoutExtras_staysResumed() {
        ActivityScenario.launch(ActivityMain::class.java).use { scenario ->
            Espresso.onIdle()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test
    fun home_showsBottomNavAndTitle() {
        ActivityScenario.launch(ActivityMain::class.java).use {
            waitUntilIdle()
            waitForTag("bottom_Home")
            composeRule.onNodeWithTag("screen_title").assertTextEquals("Home")
            composeRule.onNodeWithTag("bottom_Home").assertIsDisplayed()
            composeRule.onNodeWithTag("bottom_Catalog").assertIsDisplayed()
            composeRule.onNodeWithTag("bottom_My Bar").assertIsDisplayed()
            composeRule.onNodeWithTag("bottom_Saved").assertIsDisplayed()
            composeRule.onNodeWithTag("bottom_Mixer").assertIsDisplayed()
        }
    }

    @Test
    fun catalogTab_showsBrowseCatalog() {
        ActivityScenario.launch(ActivityMain::class.java).use {
            waitUntilIdle()
            waitForTag("bottom_Catalog")
            composeRule.onNodeWithTag("bottom_Catalog").performClick()
            waitUntilIdle()
            waitForText("Browse catalog")
            composeRule.onNodeWithText("Browse catalog").assertIsDisplayed()
            composeRule.onNodeWithText("Favourite Ingredients").assertIsDisplayed()
        }
    }

    @Test
    fun search_opensNameAndIngredientModes() {
        ActivityScenario.launch(ActivityMain::class.java).use {
            waitUntilIdle()
            waitForTag("screen_header")
            composeRule.onNodeWithContentDescription("Search").performClick()
            waitUntilIdle()
            waitForText("Drink name")
            composeRule.onNodeWithText("Drink name").assertIsDisplayed()
            composeRule.onNodeWithText("Ingredient").assertIsDisplayed()
        }
    }

    @Test
    fun settings_showsAppearance() {
        ActivityScenario.launch(ActivityMain::class.java).use {
            waitUntilIdle()
            waitForTag("screen_header")
            composeRule.onNodeWithContentDescription("Settings").performClick()
            waitUntilIdle()
            waitForText("Appearance")
            composeRule.onNodeWithText("Appearance").assertIsDisplayed()
            composeRule.onNodeWithText("System default").assertIsDisplayed()
            composeRule.onNodeWithText("Shopping list").assertIsDisplayed()
        }
    }

    @Test
    fun savedTab_showsSavedCocktails() {
        ActivityScenario.launch(ActivityMain::class.java).use {
            waitUntilIdle()
            waitForTag("bottom_Saved")
            composeRule.onNodeWithTag("bottom_Saved").performClick()
            waitUntilIdle()
            waitForText("Saved Cocktails")
            composeRule.onNodeWithText("Saved Cocktails").assertIsDisplayed()
        }
    }

    @Test
    fun drinkExtras_showsDrinkName() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, ActivityMain::class.java).putDrinkExtra(
            Drink("11007", "Margarita", ""),
        )

        ActivityScenario.launch<ActivityMain>(intent).use { scenario ->
            waitUntilIdle()
            waitForText("Margarita")
            composeRule.onNodeWithText("Margarita").assertIsDisplayed()
            scenario.onActivity { activity ->
                assertTrue(!activity.isFinishing)
            }
        }
    }

    private fun waitUntilIdle() {
        Espresso.onIdle()
        composeRule.waitForIdle()
    }

    private fun waitForText(text: String, timeoutMs: Long = 15_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String, timeoutMs: Long = 15_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
