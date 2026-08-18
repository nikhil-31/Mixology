package com.capstone.nik.mixology.ui.shopping

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.data.ShoppingItemEntity
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
class ShoppingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun empty_showsHint() {
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(),
                    onToggle = {},
                    onRemove = {},
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("No items yet. Add ingredients from a saved cocktail.")
            .assertIsDisplayed()
    }

    @Test
    fun item_toggleAndRemove() {
        val toggled = mutableListOf<Long>()
        val removed = mutableListOf<Long>()
        composeRule.setContent {
            MixologyTheme {
                ShoppingScreen(
                    state = ShoppingUiState(
                        items = listOf(ShoppingItemEntity(id = 3, name = "Lime", checked = false)),
                    ),
                    onToggle = { toggled.add(it.id) },
                    onRemove = { removed.add(it.id) },
                    onClearChecked = {},
                )
            }
        }
        composeRule.onNodeWithText("Lime").performClick()
        composeRule.onNodeWithContentDescription("Remove item").performClick()
        assertEquals(listOf(3L), toggled)
        assertEquals(listOf(3L), removed)
    }
}
