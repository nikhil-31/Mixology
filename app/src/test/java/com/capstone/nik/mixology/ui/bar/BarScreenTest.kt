package com.capstone.nik.mixology.ui.bar

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
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
class BarScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addChip_opensPickerAndCheckmarkToggles() {
        var picking = false
        val toggled = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = BarUiState(
                        loading = false,
                        picking = picking,
                        catalogTerms = listOf("Gin"),
                        visibleTerms = listOf("Gin"),
                    ),
                    onOpenPicker = { picking = true },
                    onClosePicker = { picking = false },
                    onQueryChanged = {},
                    onToggleIngredient = { toggled.add(it) },
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithTag("bar_add").performClick()
        assertEquals(true, picking)
    }

    @Test
    fun picker_showsCheckmarkAndDone() {
        val closed = mutableListOf<Boolean>()
        val toggled = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = BarUiState(
                        loading = false,
                        picking = true,
                        bar = listOf("Gin"),
                        catalogTerms = listOf("Gin", "Campari"),
                        visibleTerms = listOf("Gin", "Campari"),
                    ),
                    onOpenPicker = {},
                    onClosePicker = { closed.add(true) },
                    onQueryChanged = {},
                    onToggleIngredient = { toggled.add(it) },
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("Gin").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("In your bar").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_term_Campari").performClick()
        assertEquals(listOf("Campari"), toggled)
        composeRule.onNodeWithTag("bar_done").performClick()
        assertEquals(listOf(true), closed)
    }

    @Test
    fun drinkClick_reportsMakeableCocktail() {
        val clicked = mutableListOf<Drink>()
        val drink = Drink("11003", "Negroni", "", saved = false)
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = BarUiState(
                        loading = false,
                        bar = listOf("Gin"),
                        makeable = listOf(drink),
                        almost = listOf(
                            BarAlmostDrink(
                                Drink("11728", "Martini", ""),
                                listOf("Dry Vermouth"),
                            ),
                        ),
                    ),
                    onOpenPicker = {},
                    onClosePicker = {},
                    onQueryChanged = {},
                    onToggleIngredient = {},
                    onDrinkClick = { clicked.add(it) },
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("You can make").assertIsDisplayed()
        composeRule.onNodeWithText("Almost").assertIsDisplayed()
        composeRule.onNodeWithText("Needs Dry Vermouth").assertIsDisplayed()
        composeRule.onNodeWithText("Negroni").performClick()
        assertEquals("11003", clicked.single().id)
    }

    @Test
    fun addedIngredients_wrapOntoNextLine() {
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = BarUiState(
                        loading = false,
                        bar = listOf(
                            "Gin",
                            "Vodka",
                            "Rum",
                            "Tequila",
                            "Whiskey",
                            "Vermouth",
                            "Bitters",
                            "Aperol",
                        ),
                    ),
                    onOpenPicker = {},
                    onClosePicker = {},
                    onQueryChanged = {},
                    onToggleIngredient = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithTag("bar_chip_Gin").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_chip_Aperol").assertIsDisplayed()
    }
}
