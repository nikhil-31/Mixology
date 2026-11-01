package com.capstone.nik.mixology.ui.shopping

import android.app.Application
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.ui.model.IngredientMeasure
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
class ShoppingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty_showsHint() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("No items yet. Add ingredients from a cocktail.")
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun item_toggleAndRemove() {
        val toggled = mutableListOf<Long>()
        val removed = mutableListOf<Long>()
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(
                        sections = listOf(
                            ShoppingSection(
                                drinkId = "",
                                drink = null,
                                items = listOf(ShoppingItemEntity(id = 3, name = "Lime", checked = false)),
                            ),
                        ),
                    ),
                    onToggle = { toggled.add(it.id) },
                    onRemove = { removed.add(it.id) },
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("Lime").performClick()
        // No delete button per row any more: swipe left removes.
        composeRule.onNodeWithContentDescription("Remove item").assertDoesNotExist()
        // Swipe-to-delete is the gesture; the row also offers Remove to accessibility services.
        composeRule.onNodeWithTag("shopping_row").performCustomAccessibilityActionWithLabel("Remove item")
        assertEquals(listOf(3L), toggled)
        assertEquals(listOf(3L), removed)
        composeRule.onNodeWithText("Clear checked").assertDoesNotExist()
    }

    @Test
    fun checkedItem_showsClearChecked() {
        var cleared = 0
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(
                        sections = listOf(
                            ShoppingSection(
                                drinkId = "",
                                drink = null,
                                items = listOf(ShoppingItemEntity(id = 3, name = "Lime", checked = true)),
                            ),
                        ),
                    ),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = { cleared += 1 },
                )
            }
        }
        composeRule.onNodeWithText("Clear checked").performClick()
        assertEquals(1, cleared)
    }

    @Test
    fun section_showsDrinkNameAndMeasure() {
        val drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "",
            ingredients = listOf(IngredientMeasure("Lime juice", "1 oz")),
        )
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(
                        sections = listOf(
                            ShoppingSection(
                                drinkId = "11007",
                                drink = drink,
                                items = listOf(
                                    ShoppingItemEntity(
                                        id = 3,
                                        name = "Lime juice",
                                        checked = false,
                                        drinkId = "11007",
                                    ),
                                ),
                            ),
                        ),
                    ),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithText("Lime juice").assertIsDisplayed()
        composeRule.onNodeWithText("1 oz").assertIsDisplayed()
    }

    private fun margaritaAndDaiquiri(limeChecked: Boolean = false) = ShoppingUiState(
        sections = listOf(
            ShoppingSection(
                drinkId = "1",
                drink = Drink("1", "Margarita", "", ingredients = listOf(IngredientMeasure("Lime juice", "1 oz"))),
                items = listOf(
                    ShoppingItemEntity(id = 1, name = "Lime juice", checked = limeChecked, drinkId = "1"),
                    ShoppingItemEntity(id = 2, name = "Tequila", drinkId = "1"),
                ),
            ),
            ShoppingSection(
                drinkId = "2",
                drink = Drink("2", "Daiquiri", "", ingredients = listOf(IngredientMeasure("Lime Juice", "3/4 oz"))),
                items = listOf(ShoppingItemEntity(id = 3, name = "Lime Juice", drinkId = "2")),
            ),
        ),
    )

    @Test
    fun progress_andHeaderOpensDrink() {
        val opened = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = margaritaAndDaiquiri(limeChecked = true),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                    onDrinkClick = { opened.add(it.id) },
                )
            }
        }
        composeRule.onNodeWithText("1 of 3 checked").assertIsDisplayed()
        composeRule.onNodeWithText("Margarita").performClick()
        assertEquals(listOf("1"), opened)
    }

    @Test
    fun byIngredient_mergesSameIngredientAcrossDrinks() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = margaritaAndDaiquiri().copy(byIngredient = true),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                )
            }
        }
        composeRule.onAllNodesWithText("Lime juice").assertCountEquals(1)
        composeRule.onNodeWithText("Margarita · Daiquiri").assertIsDisplayed()
    }

    @Test
    fun empty_findRecipeCallsBack() {
        var found = 0
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                    onFindRecipe = { found += 1 },
                )
            }
        }
        composeRule.onNodeWithText("Your list is empty").assertIsDisplayed()
        composeRule.onNodeWithTag("shopping_find_recipe").performClick()
        assertTrue(found == 1)
    }
}
