package com.capstone.nik.mixology.ui.hot

import android.app.Application
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
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
class HotScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsCategoryRows_andSeeAllReportsIngredient() {
        val seeAll = mutableListOf<DrinkFilter>()
        composeRule.setContent {
            MixologyTheme {
                HotScreen(
                    state = HotUiState(
                        loading = false,
                        categories = listOf(
                            HotCategory(
                                filter = DrinkFilter.VODKA,
                                drinks = listOf(Drink("14167", "Vodka Martini", "", saved = false)),
                            ),
                            HotCategory(
                                filter = DrinkFilter.GIN,
                                drinks = listOf(Drink("11000", "Mojito", "", saved = false)),
                            ),
                        ),
                    ),
                    onDrinkClick = {},
                    onToggleSaved = {},
                    onSeeAll = { seeAll.add(it) },
                )
            }
        }

        composeRule.scrollHomeTo("Vodka").assertIsDisplayed()
        composeRule.scrollHomeTo("Gin").assertIsDisplayed()
        composeRule.scrollHomeTo("Mojito").assertIsDisplayed()
        composeRule.scrollHomeTo("Vodka")
        composeRule.onAllNodesWithText("See all")[0].performClick()
        assertEquals(listOf(DrinkFilter.VODKA), seeAll)
    }

    @Test
    fun drinkClick_reportsCocktail() {
        val clicked = mutableListOf<Drink>()
        composeRule.setContent {
            MixologyTheme {
                HotScreen(
                    state = HotUiState(
                        loading = false,
                        categories = listOf(
                            HotCategory(
                                filter = DrinkFilter.ALCOHOLIC,
                                drinks = listOf(Drink("11007", "Margarita", "", saved = false)),
                            ),
                        ),
                    ),
                    onDrinkClick = { clicked.add(it) },
                    onToggleSaved = {},
                    onSeeAll = {},
                )
            }
        }

        composeRule.onAllNodesWithText("Margarita")[0].performClick()
        assertEquals("11007", clicked.single().id)
    }

    @Test
    fun recentlyViewed_showsWithoutSeeAll() {
        val seeAll = mutableListOf<DrinkFilter>()
        composeRule.setContent {
            MixologyTheme {
                HotScreen(
                    state = HotUiState(
                        loading = false,
                        categories = listOf(
                            HotCategory(
                                filter = DrinkFilter.RECENTLY_VIEWED,
                                drinks = listOf(
                                    Drink("11007", "Margarita", "", saved = false),
                                    Drink("11003", "Negroni", "", saved = false),
                                ),
                            ),
                            HotCategory(
                                filter = DrinkFilter.GIN,
                                drinks = listOf(Drink("11000", "Mojito", "", saved = false)),
                            ),
                        ),
                    ),
                    onDrinkClick = {},
                    onToggleSaved = {},
                    onSeeAll = { seeAll.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("Pick up where you left off").assertIsDisplayed()
        composeRule.scrollHomeTo("Recently viewed").assertIsDisplayed()
        composeRule.onNodeWithText("Negroni").assertIsDisplayed()
        // The hero drink is not repeated in the recent rail.
        assertEquals(1, composeRule.onAllNodesWithText("Margarita").fetchSemanticsNodes().size)
        composeRule.scrollHomeTo("Gin")
        composeRule.onAllNodesWithText("See all").fetchSemanticsNodes().let { nodes ->
            assertEquals(1, nodes.size)
        }
        composeRule.onNodeWithText("See all").performClick()
        assertEquals(listOf(DrinkFilter.GIN), seeAll)
    }

    @Test
    fun loading_showsSkeleton() {
        composeRule.setContent {
            MixologyTheme {
                HotScreen(
                    state = HotUiState(loading = true),
                    onDrinkClick = {},
                    onToggleSaved = {},
                    onSeeAll = {},
                )
            }
        }

        composeRule.onNodeWithTag("home_skeleton").assertIsDisplayed()
    }

    @Test
    fun heroAndQuickActions_reportClicks() {
        val clicked = mutableListOf<Drink>()
        val toggled = mutableListOf<Drink>()
        var openedBar = 0
        var openedAda = 0
        composeRule.setContent {
            MixologyTheme {
                HotScreen(
                    state = HotUiState(
                        loading = false,
                        categories = listOf(
                            HotCategory(
                                filter = DrinkFilter.GIN,
                                drinks = listOf(Drink("11000", "Mojito", "", saved = false)),
                            ),
                        ),
                    ),
                    onDrinkClick = { clicked.add(it) },
                    onToggleSaved = { toggled.add(it) },
                    onSeeAll = {},
                    onOpenBar = { openedBar++ },
                    onOpenAda = { openedAda++ },
                )
            }
        }

        composeRule.onNodeWithText("Featured").assertIsDisplayed()
        composeRule.onNodeWithTag("home_view_recipe").performClick()
        composeRule.onNodeWithTag("home_hero_save").performClick()
        composeRule.onNodeWithTag("home_quick_bar").performClick()
        composeRule.onNodeWithTag("home_quick_ada").performClick()

        assertEquals("11000", clicked.single().id)
        assertEquals("11000", toggled.single().id)
        assertEquals(1, openedBar)
        assertEquals(1, openedAda)
    }
}

private fun ComposeContentTestRule.scrollHomeTo(text: String): SemanticsNodeInteraction {
    onNodeWithTag("home_list").performScrollToNode(hasText(text))
    return onAllNodesWithText(text)[0]
}
