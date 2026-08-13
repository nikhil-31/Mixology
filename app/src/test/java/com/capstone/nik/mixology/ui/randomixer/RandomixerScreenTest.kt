package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.Network.remoteModel.Drink
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
class RandomixerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsDrinkAndSwipeHint() {
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(),
                    onSave = {},
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithText("Margarita").assertIsDisplayed()
        composeRule.onNodeWithText("Alcoholic").assertIsDisplayed()
        composeRule.onNodeWithText("Swipe right to save · Swipe left to skip").assertIsDisplayed()
    }

    @Test
    fun saveButton_reportsSave() {
        var saves = 0
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(),
                    onSave = { saves += 1 },
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Save this cocktail").performClick()
        composeRule.waitForIdle()
        assertEquals(1, saves)
    }

    @Test
    fun discardButton_reportsDiscard() {
        var discards = 0
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(),
                    onSave = {},
                    onDiscard = { discards += 1 },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Discard this cocktail").performClick()
        composeRule.waitForIdle()
        assertEquals(1, discards)
    }

    private fun sampleState() = RandomixerUiState(
        drink = Drink().apply {
            idDrink = "11007"
            strDrink = "Margarita"
            strAlcoholic = "Alcoholic"
            strInstructions = "Shake and strain."
            strDrinkThumb = ""
        },
        ingredients = listOf(IngredientMeasure("Tequila", "1 1/2 oz")),
    )
}
