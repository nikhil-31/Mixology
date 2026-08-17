package com.capstone.nik.mixology.ui.search

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import org.junit.Assert.assertEquals
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
class SearchScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyResults_showsNotAvailableMessage() {
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(query = "xyz", empty = true),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onSearch = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithText("xyz").assertIsDisplayed()
        composeRule.onNodeWithText("Sorry. Drink not available in the database. Try a new search.")
            .assertIsDisplayed()
    }

    @Test
    fun loading_showsProgressIndicator() {
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(query = "gin", loading = true),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onSearch = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
    }

    @Test
    fun results_clickOpensDrink_andFavoriteToggles() {
        val clicked = mutableListOf<Drink>()
        val toggled = mutableListOf<Drink>()
        val drink = Drink("11000", "Mojito", "", saved = false)
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(
                        query = "mojito",
                        results = listOf(drink),
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onSearch = {},
                    onDrinkClick = { clicked.add(it) },
                    onToggleSaved = { toggled.add(it) },
                )
            }
        }

        composeRule.onNodeWithText("Mojito").assertIsDisplayed()
        composeRule.onNodeWithText("Instructions").assertDoesNotExist()
        composeRule.onNodeWithText("Mojito").performClick()
        composeRule.onNodeWithContentDescription("Add or delete").performClick()

        assertEquals("11000", clicked.single().id)
        assertEquals("Mojito", toggled.single().name)
        assertTrue(!toggled.single().saved)
    }

    @Test
    fun backButton_invokesCallback() {
        var back = false
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(query = "gin"),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = { back = true },
                    onSearch = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Up navigation").performClick()
        assertTrue(back)
    }

    @Test
    fun typingSecondCharacter_triggersSearch() {
        val searched = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onSearch = { searched.add(it) },
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("g")
        assertTrue(searched.isEmpty())
        composeRule.onNode(hasSetTextAction()).performTextInput("i")
        assertEquals(listOf("gi"), searched)
    }

    @Test
    fun searchIcon_submitsTypedQuery() {
        val searched = mutableListOf<String>()
        composeRule.setContent {
            MixologyTheme {
                SearchScreen(
                    state = SearchUiState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onBack = {},
                    onSearch = { searched.add(it) },
                    onDrinkClick = {},
                    onToggleSaved = {},
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("gin")
        searched.clear()
        composeRule.onNodeWithContentDescription("Search").performClick()
        assertEquals(listOf("gin"), searched)
    }
}
