package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class SignUpScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signUp_sendsCredentials_andBackNavigates() {
        var back = false
        var email: String? = null
        var password: String? = null
        composeRule.setContent {
            MixologyTheme {
                SignUpScreen(
                    onBack = { back = true },
                    onSignUp = { e, p ->
                        email = e
                        password = p
                    },
                    onPrivacyPolicy = {},
                )
            }
        }

        composeRule.onNodeWithText("Create a new account").assertIsDisplayed()
        composeRule.onNodeWithText("Email address").performTextInput("  new@mixology.test ")
        composeRule.onNodeWithText("Password").performTextInput("hunter2")
        composeRule.onNodeWithText("SIGN UP").performClick()
        composeRule.onNodeWithContentDescription("Up navigation").performClick()

        assertEquals("new@mixology.test", email)
        assertEquals("hunter2", password)
        assertTrue(back)
    }
}
