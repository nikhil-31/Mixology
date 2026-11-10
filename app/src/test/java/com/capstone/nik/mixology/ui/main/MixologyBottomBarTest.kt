package com.capstone.nik.mixology.ui.main

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
class MixologyBottomBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsHomeMyBarShoppingAndAda_andReportsSelection() {
        val selected = mutableListOf<DrawerDestination>()
        val reselected = mutableListOf<DrawerDestination>()
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.RecipesHome,
                    onDestinationSelected = { selected.add(it) },
                    onReselect = { reselected.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("My Bar").assertIsDisplayed()
        composeRule.onNodeWithText("Shopping").assertIsDisplayed()
        composeRule.onNodeWithText("Ada").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_Catalog").assertDoesNotExist()
        // Saved and Discover open from Settings; Recipes is the Home tab.
        composeRule.onNodeWithTag("bottom_Saved").assertDoesNotExist()
        composeRule.onNodeWithTag("bottom_Recipes").assertDoesNotExist()
        composeRule.onNodeWithTag("bottom_Home").performClick()
        composeRule.onNodeWithTag("bottom_My Bar").performClick()
        composeRule.onNodeWithTag("bottom_Shopping").performClick()
        composeRule.onNodeWithTag("bottom_Ada").performClick()

        // Home is the current tab, so tapping it again is a reselect (scroll to top), not a move.
        assertTrue(reselected.single() is DrawerDestination.RecipesHome)
        assertEquals(3, selected.size)
        assertTrue(selected[0] is DrawerDestination.Bar)
        assertTrue(selected[1] is DrawerDestination.Shopping)
        assertTrue(selected[2] is DrawerDestination.Ada)
    }

    @Test
    fun shoppingTab_showsWhatIsLeftToBuy() {
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.Bar,
                    onDestinationSelected = {},
                    shoppingLeft = 3,
                )
            }
        }

        composeRule.onNodeWithTag("bottom_badge_Shopping", useUnmergedTree = true)
            .assertContentDescriptionEquals("3 items to buy")
        composeRule.onNodeWithTag("bottom_badge_Home", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun shoppingBadge_capsAt99_andHidesAtZero() {
        var left by mutableIntStateOf(150)
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.Bar,
                    onDestinationSelected = {},
                    shoppingLeft = left,
                )
            }
        }

        composeRule.onNodeWithText("99+", useUnmergedTree = true).assertExists()
        left = 0
        composeRule.onNodeWithTag("bottom_badge_Shopping", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun homeTab_reportsHomeWhenComingFromCatalog() {
        val selected = mutableListOf<DrawerDestination>()
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.Catalog,
                    onDestinationSelected = { selected.add(it) },
                )
            }
        }

        composeRule.onNodeWithTag("bottom_Home").performClick()
        assertTrue(selected.single() is DrawerDestination.RecipesHome)
    }
}
