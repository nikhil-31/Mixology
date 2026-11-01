package com.capstone.nik.mixology.ui.main

import android.app.Application
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
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.RecipesHome,
                    onDestinationSelected = { selected.add(it) },
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

        assertEquals(4, selected.size)
        assertTrue(selected[0] is DrawerDestination.RecipesHome)
        assertTrue(selected[1] is DrawerDestination.Bar)
        assertTrue(selected[2] is DrawerDestination.Shopping)
        assertTrue(selected[3] is DrawerDestination.Ada)
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
