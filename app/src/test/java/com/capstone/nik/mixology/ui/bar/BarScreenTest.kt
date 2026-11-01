package com.capstone.nik.mixology.ui.bar

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
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
        composeRule.onNodeWithText("Every ingredient is in your bar").assertIsDisplayed()
        composeRule.onNodeWithText("Almost").assertIsDisplayed()
        composeRule.onNodeWithText("Uses something you have, missing 1–2").assertIsDisplayed()
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

    @Test
    fun emptyBar_showsHint() {
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = BarUiState(loading = false),
                    onOpenPicker = {},
                    onClosePicker = {},
                    onQueryChanged = {},
                    onToggleIngredient = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText(
            "Add the bottles and mixers you have, then see what you can make.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("You can make").assertDoesNotExist()
    }

    @Test
    fun chipTap_removesThroughUndoablePath() {
        val removed = mutableListOf<String>()
        val toggled = mutableListOf<String>()
        setBar(
            BarUiState(loading = false, bar = listOf("Gin")),
            onToggleIngredient = { toggled.add(it) },
            onRemoveIngredient = { removed.add(it) },
        )

        composeRule.onNodeWithTag("bar_chip_Gin").performClick()
        assertEquals(listOf("Gin"), removed)
        assertEquals(emptyList<String>(), toggled)
    }

    @Test
    fun bigBar_collapsesBehindShowAll() {
        val bar = (1..14).map { "Ingredient $it" }
        setBar(BarUiState(loading = false, bar = bar))

        composeRule.onNodeWithTag("bar_chip_Ingredient 10").assertExists()
        composeRule.onNodeWithTag("bar_chip_Ingredient 11").assertDoesNotExist()
        composeRule.onNodeWithText("Show all (14)").performClick()
        composeRule.onNodeWithTag("bar_chip_Ingredient 14").assertExists()
        composeRule.onNodeWithText("Show less").assertExists()
    }

    @Test
    fun summary_countsIngredientsAndDrinks() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin", "Campari"),
                makeable = listOf(Drink("1", "Negroni", "")),
                almost = listOf(BarAlmostDrink(Drink("2", "Martini", ""), listOf("Vermouth"))),
            ),
        )

        composeRule.onNodeWithText("2 ingredients · 1 ready · 1 almost").assertIsDisplayed()
    }

    @Test
    fun buyNextAndAlmostChips_addTheMissingIngredient() {
        val added = mutableListOf<String>()
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                almost = listOf(
                    BarAlmostDrink(Drink("1", "Martini", ""), listOf("Dry Vermouth")),
                    BarAlmostDrink(Drink("2", "Gibson", ""), listOf("Dry Vermouth")),
                    BarAlmostDrink(Drink("3", "Gin Sour", ""), listOf("Lemon", "Sugar")),
                ),
            ),
            onAddIngredient = { added.add(it) },
        )

        composeRule.onNodeWithText("Buy next").assertIsDisplayed()
        composeRule.onNodeWithText("unlocks 2 drinks").assertExists()
        composeRule.onNodeWithTag("bar_unlock_Dry Vermouth").performClick()
        composeRule.onNodeWithTag("bar_almost_add_3_Sugar").performScrollTo().performClick()
        assertEquals(listOf("Dry Vermouth", "Sugar"), added)
    }

    @Test
    fun barWithNoDrinks_showsNoMatches() {
        val opened = mutableListOf<Boolean>()
        setBar(
            BarUiState(loading = false, bar = listOf("Absinthe")),
            onOpenPicker = { opened.add(true) },
        )

        composeRule.onNodeWithText("Nothing to make yet").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_no_matches_add").performClick()
        assertEquals(listOf(true), opened)
    }

    @Test
    fun picker_whileLoading_showsPlaceholdersNotNoData() {
        setBar(BarUiState(loading = true, picking = true))

        composeRule.onNodeWithTag("bar_picker_loading").assertExists()
        composeRule.onNodeWithText("In your bar (0)").assertDoesNotExist()
    }

    @Test
    fun picker_pinsSelectedIngredientsFirst() {
        setBar(
            BarUiState(
                loading = false,
                picking = true,
                bar = listOf("Vodka"),
                catalogTerms = listOf("Gin", "Vodka"),
                visibleTerms = listOf("Gin", "Vodka"),
            ),
        )

        composeRule.onNodeWithText("In your bar (1)").assertIsDisplayed()
        composeRule.onNodeWithText("All ingredients").assertIsDisplayed()
        val vodkaTop = composeRule.onNodeWithTag("bar_term_Vodka").fetchSemanticsNode().boundsInRoot.top
        val ginTop = composeRule.onNodeWithTag("bar_term_Gin").fetchSemanticsNode().boundsInRoot.top
        assertTrue(vodkaTop < ginTop)
    }

    private fun setBar(
        state: BarUiState,
        onOpenPicker: () -> Unit = {},
        onToggleIngredient: (String) -> Unit = {},
        onRemoveIngredient: (String) -> Unit = {},
        onAddIngredient: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            MixologyTheme {
                BarScreen(
                    state = state,
                    onOpenPicker = onOpenPicker,
                    onClosePicker = {},
                    onQueryChanged = {},
                    onToggleIngredient = onToggleIngredient,
                    onDrinkClick = {},
                    onToggleSaved = {},
                    onRemoveIngredient = onRemoveIngredient,
                    onAddIngredient = onAddIngredient,
                )
            }
        }
    }
}
