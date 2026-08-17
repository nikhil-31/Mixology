package com.capstone.nik.mixology.ui.randomixer

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.capstone.nik.mixology.data.Drink
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
    fun showsDrinkAndActionButtons() {
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
        composeRule.onNodeWithContentDescription("Save this cocktail").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Discard this cocktail").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Show drink details").assertIsDisplayed()
    }

    @Test
    fun scrollingUp_revealsDrinkDetails() {
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(),
                    onSave = {},
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithTag("randomixer_card").performTouchInput { swipeUp() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Instructions").assertIsDisplayed()
        composeRule.onNodeWithText("Shake and strain.").assertIsDisplayed()
        composeRule.onNodeWithText("1 1/2 oz").assertIsDisplayed()
    }

    @Test
    fun infoButton_scrollsToDrinkDetails() {
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(),
                    onSave = {},
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Show drink details").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Instructions").assertIsDisplayed()
        composeRule.onNodeWithText("Shake and strain.").assertIsDisplayed()
    }

    @Test
    fun savedDrink_showsSavedIndicator() {
        composeRule.setContent {
            MixologyTheme {
                RandomixerScreen(
                    state = sampleState(saved = true),
                    onSave = {},
                    onDiscard = {},
                )
            }
        }

        composeRule.onNodeWithText("Saved").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Cocktail already saved").assertIsDisplayed()
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

    private fun sampleState(saved: Boolean = false) = RandomixerUiState(
        drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "",
            alcoholic = "Alcoholic",
            instructions = "Shake and strain.",
            ingredients = listOf(IngredientMeasure("Tequila", "1 1/2 oz")),
        ),
        saved = saved,
    )
}
