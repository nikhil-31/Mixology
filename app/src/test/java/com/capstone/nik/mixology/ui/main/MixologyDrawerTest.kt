package com.capstone.nik.mixology.ui.main

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class MixologyDrawerTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsFilterDestinations_andReportsSelection() {
        val selected = mutableListOf<DrawerDestination>()
        composeRule.setContent {
            MixologyTheme {
                Box(Modifier.fillMaxSize()) {
                    MixologyDrawer(
                        selectedRoute = gridRoute(DrinkFilter.ALCOHOLIC),
                        onDestinationSelected = { selected.add(it) },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Mixology").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_Saved Cocktails").assertIsDisplayed()
        composeRule.onNodeWithTag("drawer_Randomixer").performScrollTo().performClick()
        composeRule.onNodeWithTag("drawer_Gin").performScrollTo().performClick()

        assertTrue(selected[0] is DrawerDestination.Randomixer)
        assertEquals(DrinkFilter.GIN, (selected[1] as DrawerDestination.Filter).filter)
    }
}
