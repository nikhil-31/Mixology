package com.capstone.nik.mixology.ui.catalog

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
