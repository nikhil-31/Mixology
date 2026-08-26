package com.capstone.nik.mixology.ui.catalog

import android.app.Application
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.capstone.nik.mixology.repository.FilterKind
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
class CatalogScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun terms_clickOpensFilter() {
        val opened = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(
                        loading = false,
                        kind = FilterKind.INGREDIENT,
                        terms = listOf("Tequila", "Rum"),
                        visibleTerms = listOf("Tequila", "Rum"),
                    ),
                    onSelectKind = {},
                    onQueryChanged = {},
                    onOpenTerm = { opened.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("Tequila").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Tequila").assertIsDisplayed()
        composeRule.onNodeWithText("Tequila").performClick()
        assertEquals(listOf("Tequila"), opened)
    }

    @Test
    fun categoryTerms_doNotShowIngredientImages() {
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(
                        loading = false,
                        kind = FilterKind.DRINK_TYPE,
                        terms = listOf("Cocktail"),
                        visibleTerms = listOf("Cocktail"),
                    ),
                    onSelectKind = {},
                    onQueryChanged = {},
                    onOpenTerm = {},
                )
            }
        }

        composeRule.onNodeWithText("Cocktail").assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription("Cocktail").assertCountEquals(0)
    }

    @Test
    fun tabs_reportKindSelection() {
        val kinds = mutableListOf<FilterKind>()
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(loading = false, kind = FilterKind.INGREDIENT),
                    onSelectKind = { kinds.add(it) },
                    onQueryChanged = {},
                    onOpenTerm = {},
                )
            }
        }

        composeRule.onNodeWithText("Favourite Ingredients").assertIsDisplayed()
        composeRule.onNodeWithText("Category").performClick()
        composeRule.onNodeWithText("Glass").performClick()
        composeRule.onNodeWithText("Alcoholic").performClick()
        assertEquals(
            listOf(FilterKind.DRINK_TYPE, FilterKind.GLASS, FilterKind.ALCOHOL),
            kinds,
        )
    }

    @Test
    fun loading_showsProgressIndicator() {
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(loading = true),
                    onSelectKind = {},
                    onQueryChanged = {},
                    onOpenTerm = {},
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun emptyTerms_showsNotAvailableMessage() {
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(loading = false),
                    onSelectKind = {},
                    onQueryChanged = {},
                    onOpenTerm = {},
                )
            }
        }

        composeRule.onNodeWithText("Sorry. Drink not available in the database. Try a new search.")
            .assertIsDisplayed()
    }

    @Test
    fun query_reportsChangedText() {
        val queries = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                CatalogScreen(
                    state = CatalogUiState(
                        loading = false,
                        terms = listOf("Gin"),
                        visibleTerms = listOf("Gin"),
                    ),
                    onSelectKind = {},
                    onQueryChanged = { queries.add(it) },
                    onOpenTerm = {},
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("gi")
        assertEquals(listOf("gi"), queries)
    }
}
