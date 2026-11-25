package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class AdaRecipeCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun show(state: AdaRecipe.State) {
        composeRule.setContent {
            MixologyTheme {
                AdaRecipeCard(
                    recipe = AdaRecipe(
                        name = "Paloma",
                        photoUrl = null,
                        source = "instagram",
                        sourceUrl = "https://www.instagram.com/p/abc/",
                        ingredients = listOf(IngredientMeasure("Tequila", "2 oz")),
                        instructions = null,
                        state = state,
                        recipeId = "u-1".takeIf { state == AdaRecipe.State.Saved },
                    ),
                    canAnswer = true,
                    onSave = {},
                    onDrop = {},
                    onOpen = {},
                    onEdit = {},
                )
            }
        }
    }

    @Test
    fun aDraftIsMarkedBetaWithANote() {
        show(AdaRecipe.State.Draft)
        composeRule.onNodeWithTag("beta_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("ada_recipe_beta").assertIsDisplayed()
        composeRule.onNodeWithTag("ada_recipe_edit").assertIsDisplayed()
    }

    @Test
    fun aSavedRecipeKeepsTheBadgeButNotTheNote() {
        show(AdaRecipe.State.Saved)
        composeRule.onNodeWithTag("beta_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("ada_recipe_beta").assertDoesNotExist()
    }
}
