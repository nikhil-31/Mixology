package com.capstone.nik.mixology.ui.bar

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.Network.ConnectionError
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarSubstitute
import com.capstone.nik.mixology.repository.BarTerm
import com.capstone.nik.mixology.repository.BarUnlock
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
                        catalogTerms = terms("Gin"),
                        visibleTerms = terms("Gin"),
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
                        catalogTerms = terms("Gin", "Campari"),
                        visibleTerms = terms("Gin", "Campari"),
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
    fun onlyTheChipsRemoveButtonRemoves() {
        val removed = mutableListOf<String>()
        val toggled = mutableListOf<String>()
        setBar(
            BarUiState(loading = false, bar = listOf("Gin")),
            onToggleIngredient = { toggled.add(it) },
            onRemoveIngredient = { removed.add(it) },
        )

        composeRule.onNodeWithTag("bar_chip_Gin").performClick()
        assertEquals(emptyList<String>(), removed)
        composeRule.onNodeWithContentDescription("Remove Gin").performClick()
        assertEquals(listOf("Gin"), removed)
        assertEquals(emptyList<String>(), toggled)
    }

    @Test
    fun chips_areGroupedByShelf_whenThereIsMoreThanOne() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Campari", "Gin", "Lime"),
                barGroups = listOf(
                    BarGroup(BarCategory.SPIRIT, listOf("Gin")),
                    BarGroup(BarCategory.LIQUEUR, listOf("Campari")),
                    BarGroup(BarCategory.FRESH, listOf("Lime")),
                ),
            ),
        )

        composeRule.onNodeWithText("Spirits").assertIsDisplayed()
        composeRule.onNodeWithText("Liqueurs").assertIsDisplayed()
        composeRule.onNodeWithText("Fresh").assertIsDisplayed()
        val ginTop = composeRule.onNodeWithTag("bar_chip_Gin").fetchSemanticsNode().boundsInRoot.top
        val campariTop = composeRule.onNodeWithTag("bar_chip_Campari").fetchSemanticsNode().boundsInRoot.top
        assertTrue(ginTop < campariTop)
    }

    @Test
    fun chips_onOneShelf_haveNoShelfName() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin", "Vodka"),
                barGroups = listOf(BarGroup(BarCategory.SPIRIT, listOf("Gin", "Vodka"))),
            ),
        )

        composeRule.onNodeWithText("Spirits").assertDoesNotExist()
        composeRule.onNodeWithTag("bar_chip_Vodka").assertIsDisplayed()
    }

    @Test
    fun failedRecommendations_showAnErrorNotNothingToMake() {
        val retried = mutableListOf<Boolean>()
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                recommendationsError = ConnectionError.NO_CONNECTION,
            ),
            onRetry = { retried.add(true) },
        )

        composeRule.onNodeWithTag("bar_recommendations_error").assertExists()
        composeRule.onNodeWithText("Nothing to make yet").assertDoesNotExist()
        composeRule.onNodeWithText("1 ingredient").assertIsDisplayed()
    }

    @Test
    fun drinksWithAStandInForEverythingMissing_getTheirOwnSection() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin", "Aperol", "Sweet Vermouth"),
                almost = listOf(
                    BarAlmostDrink(
                        Drink("1", "Negroni", ""),
                        listOf("Campari"),
                        listOf(BarSubstitute("Campari", "Aperol", "lighter and sweeter")),
                    ),
                    BarAlmostDrink(Drink("2", "Martini", ""), listOf("Dry Vermouth")),
                ),
            ),
        )

        composeRule.onNodeWithText("With a swap").assertIsDisplayed()
        composeRule.onNodeWithText("Use Aperol for Campari · lighter and sweeter").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_almost_add_1_Campari").assertDoesNotExist()
        composeRule.onNodeWithText("Needs Dry Vermouth").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun longMakeableList_showsEightUntilSeeAll() {
        val drinks = (1..11).map { Drink("$it", "Drink $it", "") }
        setBar(BarUiState(loading = false, bar = listOf("Gin"), makeable = drinks))

        composeRule.onNodeWithTag("bar_list").performScrollToNode(hasTestTag("bar_makeable_see_all"))
        composeRule.onNodeWithText("Drink 9").assertDoesNotExist()
        composeRule.onNodeWithText("See all 11").performClick()
        composeRule.onNodeWithTag("bar_list").performScrollToNode(hasText("Drink 9"))
        composeRule.onNodeWithText("Drink 9").assertExists()
    }

    @Test
    fun ownRecipes_areMarkedAmongTheDrinksToMake() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                makeable = listOf(Drink("u-1", "House Sour", ""), Drink("2", "Martini", "")),
            ),
        )

        composeRule.onNodeWithText("Your recipe").assertIsDisplayed()
    }

    @Test
    fun buyNext_saysGetsYouCloser_whenNothingIsFinishedAlone() {
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                unlocks = listOf(BarUnlock("Mint", count = 0, nearly = 3)),
            ),
        )

        composeRule.onNodeWithText("Gets you closer to 3 drinks").assertExists()
    }

    @Test
    fun askAdaAndPhoto_areOfferedUnderTheBar() {
        val asked = mutableListOf<String>()
        setBar(
            BarUiState(loading = false, bar = listOf("Gin")),
            onAskAda = { asked.add("ada") },
            onAddFromPhoto = { asked.add("photo") },
        )

        composeRule.onNodeWithTag("bar_ask_ada").performClick()
        composeRule.onNodeWithTag("bar_add_photo").performClick()
        assertEquals(listOf("ada", "photo"), asked)
    }

    @Test
    fun emptyBar_offersAPhotoToo() {
        val asked = mutableListOf<String>()
        setBar(BarUiState(loading = false), onAddFromPhoto = { asked.add("photo") })

        composeRule.onNodeWithTag("bar_empty_photo").performClick()
        assertEquals(listOf("photo"), asked)
    }

    @Test
    fun picker_listsPopularThenShelves_andNarrowsToOne() {
        val catalog = listOf(
            BarTerm("Gin", BarCategory.SPIRIT, 900),
            BarTerm("Campari", BarCategory.LIQUEUR, 200),
            BarTerm("Lime", BarCategory.FRESH, 50),
        )
        val categories = mutableListOf<String?>()
        setBar(
            BarUiState(loading = false, picking = true, catalogTerms = catalog, visibleTerms = catalog),
            onPickerCategory = { categories.add(it) },
        )

        composeRule.onNodeWithText("Popular").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_popular_Gin").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_term_Gin").assertExists()
        composeRule.onNodeWithTag("bar_picker_category_liqueur").performClick()
        assertEquals(listOf<String?>(BarCategory.LIQUEUR), categories)
    }

    @Test
    fun picker_offersTheQueryAsACustomIngredient() {
        val toggled = mutableListOf<String>()
        setBar(
            BarUiState(
                loading = false,
                picking = true,
                query = "Tanqueray",
                catalogTerms = terms("Gin"),
                visibleTerms = emptyList(),
            ),
            onToggleIngredient = { toggled.add(it) },
        )

        composeRule.onNodeWithText("Add “Tanqueray”").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_add_custom").performClick()
        assertEquals(listOf("Tanqueray"), toggled)
    }

    @Test
    fun picker_withAQueryThatNamesAnIngredient_offersNoCustomRow() {
        setBar(
            BarUiState(
                loading = false,
                picking = true,
                query = "gin",
                catalogTerms = terms("Gin", "Ginger ale"),
                visibleTerms = terms("Gin", "Ginger ale"),
            ),
        )

        composeRule.onNodeWithTag("bar_add_custom").assertDoesNotExist()
        composeRule.onNodeWithTag("bar_term_Ginger ale").assertIsDisplayed()
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
    fun buyNextAndAlmostChips_askWhereTheMissingIngredientGoes() {
        val chosen = mutableListOf<BarAddChoice>()
        val martini = Drink("1", "Martini", "")
        val ginSour = Drink("3", "Gin Sour", "")
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                almost = listOf(
                    BarAlmostDrink(martini, listOf("Dry Vermouth")),
                    BarAlmostDrink(Drink("2", "Gibson", ""), listOf("Dry Vermouth")),
                    BarAlmostDrink(ginSour, listOf("Lemon", "Sugar")),
                ),
                unlocks = listOf(BarUnlock("Dry Vermouth", 2)),
            ),
            onChooseAdd = { chosen.add(it) },
        )

        composeRule.onNodeWithText("Buy next").assertIsDisplayed()
        composeRule.onNodeWithText("unlocks 2 drinks").assertExists()
        composeRule.onNodeWithTag("bar_unlock_Dry Vermouth").performClick()
        composeRule.onNodeWithTag("bar_almost_add_3_Sugar").performScrollTo().performClick()
        assertEquals(
            listOf(
                BarAddChoice("Dry Vermouth", unlockCount = 2),
                BarAddChoice("Sugar", drink = ginSour),
            ),
            chosen,
        )
    }

    @Test
    fun addSheet_addsToTheBar() {
        val added = mutableListOf<String>()
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                addChoice = BarAddChoice("Sugar", drink = Drink("3", "Gin Sour", "")),
            ),
            onAddIngredient = { added.add(it) },
        )

        composeRule.onNodeWithText("Needed for Gin Sour").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_add_sheet_bar").performClick()
        assertEquals(listOf("Sugar"), added)
    }

    @Test
    fun addSheet_addsToTheShoppingListUnderTheDrink() {
        val shopping = mutableListOf<Pair<String, String>>()
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                addChoice = BarAddChoice("Sugar", drink = Drink("3", "Gin Sour", "")),
            ),
            onAddToShopping = { name, drinkId -> shopping.add(name to drinkId) },
        )

        composeRule.onNodeWithTag("bar_add_sheet_shopping").performClick()
        assertEquals(listOf("Sugar" to "3"), shopping)
    }

    @Test
    fun addSheet_fromBuyNext_showsUnlocksAndHasNoDrink() {
        val shopping = mutableListOf<Pair<String, String>>()
        setBar(
            BarUiState(
                loading = false,
                bar = listOf("Gin"),
                addChoice = BarAddChoice("Dry Vermouth", unlockCount = 2),
            ),
            onAddToShopping = { name, drinkId -> shopping.add(name to drinkId) },
        )

        composeRule.onNodeWithText("unlocks 2 drinks").assertIsDisplayed()
        composeRule.onNodeWithTag("bar_add_sheet_shopping").performClick()
        assertEquals(listOf("Dry Vermouth" to ""), shopping)
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
                catalogTerms = terms("Gin", "Vodka"),
                visibleTerms = terms("Gin", "Vodka"),
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
        onChooseAdd: (BarAddChoice) -> Unit = {},
        onAddToShopping: (String, String) -> Unit = { _, _ -> },
        onRetry: () -> Unit = {},
        onPickerCategory: (String?) -> Unit = {},
        onAskAda: () -> Unit = {},
        onAddFromPhoto: () -> Unit = {},
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
                    onChooseAdd = onChooseAdd,
                    onAddToShopping = onAddToShopping,
                    onRetry = onRetry,
                    onPickerCategory = onPickerCategory,
                    onAskAda = onAskAda,
                    onAddFromPhoto = onAddFromPhoto,
                )
            }
        }
    }

    private fun terms(vararg names: String) = names.map { BarTerm(it) }
}
