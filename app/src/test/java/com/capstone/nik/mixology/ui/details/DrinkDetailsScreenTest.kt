package com.capstone.nik.mixology.ui.details

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.theme.MixologyTheme
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
class DrinkDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun scaffold_showsShareAndBack() {
        var back = false
        var share = false
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsScaffold(
                    showUpNavigation = true,
                    onBack = { back = true },
                    onShare = { share = true },
                    snackbarHostState = remember { SnackbarHostState() },
                    content = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        composeRule.onNodeWithContentDescription("Share").performClick()

        assertTrue(back)
        assertTrue(share)
    }

    @Test
    fun content_showsRecipe_andFavoriteClick() {
        var toggled = false
        val drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "",
            alcoholic = "Alcoholic",
            glass = "Cocktail glass",
            category = "Ordinary Drink",
            iba = "Contemporary Classics",
            instructions = "Shake and strain.",
            ingredients = listOf(IngredientMeasure("Tequila", "1 1/2 oz")),
        )
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsContent(
                    state = DrinkDetailsUiState(drink = drink, saved = false),
                    onToggleSaved = { toggled = true },
                )
            }
        }

        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Save cocktail").performClick()
        composeRule.onNodeWithText("Alcoholic").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Cocktail glass").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ordinary Drink").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Contemporary Classics").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("INSTRUCTIONS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Shake and strain.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("INGREDIENTS").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Tequila").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("1 1/2 oz").performScrollTo().assertIsDisplayed()

        assertTrue(toggled)
    }

    @Test
    fun loading_showsProgressIndicator() {
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsContent(
                    state = DrinkDetailsUiState(
                        loading = true,
                        drink = Drink("11007", "Margarita", ""),
                    ),
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun content_numberedInstructions_showsSteps() {
        val drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "",
            instructions = "1. Shake with ice. 2. Strain into the glass.",
        )
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsContent(
                    state = DrinkDetailsUiState(drink = drink),
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("Shake with ice.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Strain into the glass.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun heroImage_clickOpensFullscreen_andTapDismisses() {
        val drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "https://example.com/m.jpg",
            instructions = "Shake.",
        )
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsContent(
                    state = DrinkDetailsUiState(drink = drink),
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Image of the cocktail").performClick()
        composeRule.onNodeWithContentDescription("Full screen cocktail image").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        composeRule.onNodeWithContentDescription("Full screen cocktail image").assertDoesNotExist()
    }
}
