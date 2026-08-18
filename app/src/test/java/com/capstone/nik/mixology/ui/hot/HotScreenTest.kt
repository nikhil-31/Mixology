package com.capstone.nik.mixology.ui.hot

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText("Vodka").assertIsDisplayed()
        composeRule.onNodeWithText("Gin").assertIsDisplayed()
        composeRule.onNodeWithText("Mojito").assertIsDisplayed()
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

        composeRule.onNodeWithText("Margarita").performClick()
        assertEquals("11007", clicked.single().id)
    }
}
