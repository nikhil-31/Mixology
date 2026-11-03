package com.capstone.nik.mixology.ui.recipes

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.capstone.nik.mixology.ui.theme.MixologyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h891dp-xhdpi")
class ImportRecipeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<ImportRecipeIntent>()

    private fun show(state: ImportRecipeUiState) {
        composeRule.setContent {
            MixologyTheme {
                ImportRecipeScreen(state = state, onIntent = { intents += it }, onBack = {})
            }
        }
    }

    @Test
    fun importIsOffUntilThereIsText() {
        show(ImportRecipeUiState())
        composeRule.onNodeWithTag("recipe_import_submit").assertIsNotEnabled()
        composeRule.onNodeWithTag("recipe_import_paste").assertIsDisplayed()
        composeRule.onNodeWithTag("recipe_import_tip").assertIsDisplayed()
    }

    @Test
    fun aLinkShowsItsSite() {
        show(ImportRecipeUiState(text = "Look https://www.instagram.com/reel/abc/"))
        composeRule.onNodeWithTag("recipe_import_submit").assertIsEnabled()
        composeRule.onNodeWithTag("recipe_import_detected", useUnmergedTree = true).assertTextContains("Instagram link")
        composeRule.onNodeWithTag("recipe_import_clear").assertIsDisplayed()
    }

    @Test
    fun anUnreachablePostOffersToPasteTheCaption() {
        show(ImportRecipeUiState(text = "https://x.com/a/status/1", error = ImportRecipeError.Unreachable))
        composeRule.onNodeWithTag("recipe_import_error").assertIsDisplayed()
        composeRule.onNodeWithText("Paste caption").performClick()
        assertEquals(listOf<ImportRecipeIntent>(ImportRecipeIntent.DismissError), intents)
    }

    @Test
    fun importIsMarkedBeta() {
        show(ImportRecipeUiState())
        composeRule.onNodeWithTag("beta_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("recipe_import_beta").assertIsDisplayed()
    }

    @Test
    fun importingSaysItsBeta() {
        show(ImportRecipeUiState(text = "https://www.tiktok.com/@bar/video/1", importing = true))
        composeRule.onNodeWithTag("recipe_import_progress_beta").assertIsDisplayed()
    }

    @Test
    fun importingShowsProgressWithCancel() {
        show(ImportRecipeUiState(text = "https://www.tiktok.com/@bar/video/1", importing = true))
        composeRule.onNodeWithTag("recipe_import_progress").assertIsDisplayed()
        composeRule.onNodeWithText("From TikTok").assertIsDisplayed()
        composeRule.onNodeWithTag("recipe_import_cancel").performClick()
        assertEquals(listOf<ImportRecipeIntent>(ImportRecipeIntent.Cancel), intents)
    }

    @Test
    fun linkSiteNamesSocialSitesAndHosts() {
        assertEquals("YouTube", linkSite("https://youtu.be/xyz"))
        assertEquals("liquor.com", linkSite("see https://www.liquor.com/recipes/paper-plane/ ok"))
        assertNull(linkSite("2 oz gin, 1.5oz lemon"))
    }
}
