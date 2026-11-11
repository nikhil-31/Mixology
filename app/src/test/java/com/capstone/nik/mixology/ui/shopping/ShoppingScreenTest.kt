package com.capstone.nik.mixology.ui.shopping

import android.app.Application
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
import androidx.compose.ui.layout.positionInRoot
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
    fun checkedItem_showsDoneShopping_andClearCheckedInOverflow() {
        var cleared = 0
        var reviewed = 0
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
                    onOpenReview = { reviewed += 1 },
                )
            }
        }
        composeRule.onNodeWithText("Done shopping").performClick()
        assertEquals(1, reviewed)
        composeRule.onNodeWithTag("shopping_overflow").performClick()
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
        // "Margarita" also captions its checked item further down, so tap the header itself.
        composeRule.onNodeWithTag("shopping_section_1").performClick()
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

    @Test
    fun nothingChecked_hasNoDoneShopping() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(state = margaritaAndDaiquiri(), onToggle = {}, onRemove = {}, onClearChecked = {})
            }
        }
        composeRule.onNodeWithTag("shopping_done").assertDoesNotExist()
    }

    @Test
    fun reviewSheet_listsRows_marksBarItems_andConfirms() {
        var confirmed = 0
        val toggled = mutableListOf<String>()
        val lime = ReviewRow("lime juice", "Lime juice", listOf("Margarita"), emptyList(), inBar = false)
        val ice = ReviewRow("ice", "Ice", emptyList(), emptyList(), inBar = true)
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = margaritaAndDaiquiri(limeChecked = true).copy(
                        review = ReviewUi(listOf(lime, ice), selected = setOf("lime juice")),
                    ),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                    onToggleReviewRow = { toggled.add(it) },
                    onConfirmReview = { confirmed += 1 },
                )
            }
        }
        composeRule.onNodeWithText("Add to My Bar?").assertIsDisplayed()
        composeRule.onNodeWithText("Already in your bar").assertIsDisplayed()
        composeRule.onNodeWithTag("review_row_ice").performClick()
        composeRule.onNodeWithTag("review_row_lime juice").performClick()
        assertEquals(listOf("lime juice"), toggled)
        composeRule.onNodeWithText("Add 1 to My Bar").performClick()
        assertEquals(1, confirmed)
    }

    @Test
    fun checkedItems_moveToABottomSection_withTheirDrink() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = margaritaAndDaiquiri(limeChecked = true),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithTag("shopping_checked_header").performScrollTo().assertIsDisplayed()
        val header = composeRule.onNodeWithTag("shopping_checked_header").fetchSemanticsNode().positionInRoot.y
        val checkedLime = composeRule.onNodeWithText("Lime juice").fetchSemanticsNode().positionInRoot.y
        val tequila = composeRule.onNodeWithText("Tequila").fetchSemanticsNode().positionInRoot.y
        assertTrue(tequila < header)
        assertTrue(checkedLime > header)
    }

    @Test
    fun overflowOffersHistory_andNotNowSkips() {
        var history = 0
        var skipped = 0
        var dismissed = 0
        val lime = ReviewRow("lime juice", "Lime juice", listOf("Margarita"), emptyList(), inBar = false)
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = margaritaAndDaiquiri(limeChecked = true).copy(
                        review = ReviewUi(listOf(lime), selected = setOf("lime juice")),
                    ),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                    onOpenHistory = { history += 1 },
                    onSkipReview = { skipped += 1 },
                    onDismissReview = { dismissed += 1 },
                )
            }
        }
        composeRule.onNodeWithTag("review_not_now").performClick()
        assertEquals(1, skipped)
        assertEquals(0, dismissed)
    }

    @Test
    fun nothingChecked_hasNoOverflowMenu_historyLivesInTheHeader() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(state = margaritaAndDaiquiri(), onToggle = {}, onRemove = {}, onClearChecked = {})
            }
        }
        composeRule.onNodeWithTag("shopping_overflow").assertDoesNotExist()
        composeRule.onNodeWithText("Shopping history").assertDoesNotExist()
    }

    @Test
    fun emptyList_stillOffersShoppingHistory() {
        var history = 0
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                    onOpenHistory = { history += 1 },
                )
            }
        }
        composeRule.onNodeWithTag("shopping_empty_history").performClick()
        assertEquals(1, history)
    }

    private val margaritaSection = ShoppingSection(
        drinkId = "1",
        drink = Drink("1", "Margarita", "", ingredients = listOf(IngredientMeasure("Tequila", "2 oz"))),
        items = listOf(
            ShoppingItemEntity(id = 1, name = "Tequila", drinkId = "1"),
            ShoppingItemEntity(id = 2, name = "Lime", drinkId = "1"),
            ShoppingItemEntity(id = 3, name = "Salt", checked = true, drinkId = "1"),
        ),
    )

    @Test
    fun addField_submitsTrimmedText_andClears() {
        val added = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(sections = listOf(margaritaSection)),
                    onToggle = {}, onRemove = {}, onClearChecked = {},
                    onAddItem = { added.add(it) },
                )
            }
        }
        composeRule.onNodeWithTag("shopping_add_field").performTextInput("Cups")
        composeRule.onNodeWithTag("shopping_add_button").performClick()
        assertEquals(listOf("Cups"), added)
        composeRule.onNodeWithText("Cups").assertDoesNotExist()
    }

    @Test
    fun ownedItems_areMarked_andTheBannerRemovesThem() {
        var removedOwned = 0
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(sections = listOf(margaritaSection), bar = setOf("tequila")),
                    onToggle = {}, onRemove = {}, onClearChecked = {},
                    onRemoveOwned = { removedOwned++ },
                )
            }
        }
        composeRule.onNodeWithText("In your bar").assertIsDisplayed()
        composeRule.onNodeWithText("1 item is already in your bar").assertIsDisplayed()
        composeRule.onNodeWithTag("shopping_remove_owned").performClick()
        assertEquals(1, removedOwned)
    }

    @Test
    fun drinkHeader_showsWhatsLeft_andCheckAll() {
        var checked: String? = null
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(sections = listOf(margaritaSection), byIngredient = false),
                    onToggle = {}, onRemove = {}, onClearChecked = {},
                    onCheckDrink = { checked = it },
                )
            }
        }
        composeRule.onNodeWithText("2 left").assertIsDisplayed()
        composeRule.onNodeWithTag("shopping_section_menu_1").performClick()
        composeRule.onNodeWithTag("shopping_check_all").performClick()
        assertEquals("1", checked)
    }

    @Test
    fun checkedSection_collapses() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(sections = listOf(margaritaSection), byIngredient = false),
                    onToggle = {}, onRemove = {}, onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("Checked (1)").assertIsDisplayed()
        composeRule.onNodeWithText("Salt").assertExists()
        composeRule.onNodeWithTag("shopping_checked_header").performClick()
        composeRule.onNodeWithText("Salt").assertDoesNotExist()
    }

    @Test
    fun byIngredient_groupsIntoAisles_andCountsDrinks() {
        val daiquiri = ShoppingSection(
            "2", Drink("2", "Daiquiri", ""), listOf(ShoppingItemEntity(id = 4, name = "Lime", drinkId = "2")),
        )
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(
                        sections = listOf(margaritaSection, daiquiri),
                        byIngredient = true,
                        terms = mapOf("tequila" to "spirit", "lime" to "fresh"),
                    ),
                    onToggle = {}, onRemove = {}, onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithTag("shopping_aisle_spirit").assertIsDisplayed()
        composeRule.onNodeWithTag("shopping_aisle_fresh").assertIsDisplayed()
        composeRule.onNodeWithText("2 drinks").assertIsDisplayed()
        val spirit = composeRule.onNodeWithTag("shopping_aisle_spirit").fetchSemanticsNode().positionInRoot.y
        val fresh = composeRule.onNodeWithTag("shopping_aisle_fresh").fetchSemanticsNode().positionInRoot.y
        assertTrue(spirit < fresh)
    }
}
