package com.capstone.nik.mixology.ui.grid

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.data.DrinkListItem
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
class DrinkGridScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedFilter_showsEmptyMessage_whenThereAreNoDrinks() {
        composeRule.setContent {
            MixologyTheme {
                DrinkGridScreen(
                    filter = DrinkFilter.SAVED,
                    drinks = emptyList(),
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("Add a drink !!! Its lonely here.").assertIsDisplayed()
    }

    @Test
    fun alcoholicFilter_doesNotShowEmptyMessage_whenListIsEmpty() {
        composeRule.setContent {
            MixologyTheme {
                DrinkGridScreen(
                    filter = DrinkFilter.ALCOHOLIC,
                    drinks = emptyList(),
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("Add a drink !!! Its lonely here.").assertDoesNotExist()
    }

    @Test
    fun showsDrinkNames_andClickSelectsCocktail() {
        val clicked = mutableListOf<Cocktail>()
        composeRule.setContent {
            MixologyTheme {
                DrinkGridScreen(
                    filter = DrinkFilter.ALCOHOLIC,
                    drinks = listOf(
                        DrinkListItem("11007", "Margarita", "", saved = false),
                        DrinkListItem("11000", "Mojito", "", saved = true),
                    ),
                    onDrinkClick = { clicked.add(it) },
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithText("Mojito").assertIsDisplayed()
        composeRule.onNodeWithText("Margarita").performClick()

        assertEquals(1, clicked.size)
        assertEquals("11007", clicked[0].getmDrinkId())
        assertEquals("Margarita", clicked[0].getmDrinkName())
    }

    @Test
    fun favoriteButton_togglesSavedDrink() {
        val toggled = mutableListOf<DrinkListItem>()
        composeRule.setContent {
            MixologyTheme {
                DrinkGridScreen(
                    filter = DrinkFilter.ALCOHOLIC,
                    drinks = listOf(DrinkListItem("11007", "Margarita", "", saved = false)),
                    onDrinkClick = {},
                    onToggleSaved = { toggled.add(it) },
                )
            }
        }

        composeRule.onAllNodesWithText("Margarita").assertCountEquals(1)
        composeRule.onNodeWithContentDescription("Add or delete").performClick()

        assertEquals(1, toggled.size)
        assertEquals("11007", toggled[0].id)
        assertTrue(!toggled[0].saved)
    }
}
