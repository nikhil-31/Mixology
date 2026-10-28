package com.capstone.nik.mixology.ui.ada

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class AdaScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mic_hiddenWithoutSpeechRecognition() {
        show(AdaUiState(signedIn = true), voiceAvailable = false)

        composeRule.onNodeWithTag("ada_mic").assertDoesNotExist()
    }

    @Test
    fun mic_tapAsksToListen() {
        var toggles = 0
        show(AdaUiState(signedIn = true), onToggleVoice = { toggles++ })

        composeRule.onNodeWithContentDescription("Talk to Ada").performClick()

        assertEquals(1, toggles)
    }

    @Test
    fun mic_listeningShowsStopAndPlaceholder() {
        show(AdaUiState(signedIn = true, listening = true))

        composeRule.onNodeWithContentDescription("Stop listening").assertIsDisplayed()
        composeRule.onNodeWithText("Listening…").assertIsDisplayed()
    }

    @Test
    fun mic_hiddenWhileSending() {
        show(AdaUiState(signedIn = true, sending = true))

        composeRule.onNodeWithTag("ada_mic").assertDoesNotExist()
    }

    @Test
    fun denied_offersSettings() {
        var opened = 0
        show(AdaUiState(signedIn = true, voiceProblem = AdaVoiceProblem.Denied), onOpenSettings = { opened++ })

        composeRule.onNodeWithTag("ada_mic_denied").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()

        assertEquals(1, opened)
    }

    private fun show(
        state: AdaUiState,
        voiceAvailable: Boolean = true,
        onToggleVoice: () -> Unit = {},
        onOpenSettings: () -> Unit = {},
    ) {
        composeRule.setContent {
            MixologyTheme {
                AdaScreen(
                    state = state,
                    onDraftChanged = {},
                    onSend = {},
                    onSuggestion = {},
                    onDrinkClick = {},
                    onToggleSaved = {},
                    voiceAvailable = voiceAvailable,
                    onToggleVoice = onToggleVoice,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }
}
