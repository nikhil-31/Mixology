package com.capstone.nik.mixology.ui.main

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.data.DrinkFilter
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
    fun showsHomeCatalogMyBarSavedAndRandomixer_andReportsSelection() {
        val selected = mutableListOf<DrawerDestination>()
        composeRule.setContent {
            MixologyTheme {
                MixologyBottomBar(
                    currentDestination = DrawerDestination.Hot,
                    onDestinationSelected = { selected.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("Home").assertIsDisplayed()
        composeRule.onNodeWithText("Catalog").assertIsDisplayed()
        composeRule.onNodeWithText("My Bar").assertIsDisplayed()
        composeRule.onNodeWithText("Saved").assertIsDisplayed()
        composeRule.onNodeWithText("Mixer").assertIsDisplayed()
        composeRule.onAllNodesWithText("Settings").assertCountEquals(0)
        composeRule.onNodeWithTag("bottom_Catalog").performClick()
        composeRule.onNodeWithTag("bottom_My Bar").performClick()
        composeRule.onNodeWithTag("bottom_Saved").performClick()
        composeRule.onNodeWithTag("bottom_Mixer").performClick()

        assertTrue(selected[0] is DrawerDestination.Catalog)
        assertTrue(selected[1] is DrawerDestination.Bar)
        assertEquals(DrinkFilter.SAVED, (selected[2] as DrawerDestination.Filter).filter)
        assertTrue(selected[3] is DrawerDestination.Randomixer)
    }
}
