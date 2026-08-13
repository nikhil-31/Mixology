package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class PasswordChangeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun resetButton_sendsTrimmedEmail() {
        var resetEmail: String? = null
        composeRule.setContent {
            MixologyTheme {
                PasswordChangeScreen(
                    onBack = {},
                    onReset = { resetEmail = it },
                )
            }
        }

        composeRule.onNodeWithText("Enter email to get reset password message").assertIsDisplayed()
        composeRule.onNodeWithText("Username").performTextInput("  reset@mixology.test ")
        composeRule.onNodeWithText("Reset Password").performClick()

        assertEquals("reset@mixology.test", resetEmail)
    }
}
