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
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.Network.remoteModel.Drink
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
    fun scaffold_showsTitleShareAndBack() {
        var back = false
        var share = false
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsScaffold(
                    title = "Margarita",
                    showUpNavigation = true,
                    onBack = { back = true },
                    onShare = { share = true },
                    snackbarHostState = remember { SnackbarHostState() },
                    content = {},
                )
            }
        }

        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        composeRule.onNodeWithContentDescription("Share").performClick()

        assertTrue(back)
        assertTrue(share)
    }

    @Test
    fun content_showsRecipe_andFavoriteClick() {
        var toggled = false
        val drink = Drink().apply {
            idDrink = "11007"
            strDrink = "Margarita"
            strAlcoholic = "Alcoholic"
            strInstructions = "Shake and strain."
            strDrinkThumb = ""
        }
        composeRule.setContent {
            MixologyTheme {
                DrinkDetailsContent(
                    state = DrinkDetailsUiState(
                        cocktail = Cocktail("11007", "Margarita", ""),
                        drink = drink,
                        ingredients = listOf(IngredientMeasure("Tequila", "1 1/2 oz")),
                        saved = false,
                    ),
                    onToggleSaved = { toggled = true },
                )
            }
        }

        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Add or delete").performClick()
        composeRule.onNodeWithText("Alcoholic").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Instructions").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Shake and strain.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Ingredients").performScrollTo().assertIsDisplayed()
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
                        cocktail = Cocktail("11007", "Margarita", ""),
                    ),
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }
}
