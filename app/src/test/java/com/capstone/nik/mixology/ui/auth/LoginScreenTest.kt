package com.capstone.nik.mixology.ui.auth

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsAppName_andPrimaryActions() {
        composeRule.setContent {
            MixologyTheme {
                LoginScreen(
                    onLogin = { _, _ -> },
                    onForgotPassword = {},
                    onSignUp = {},
                    onGoogleSignIn = {},
                    onPrivacyPolicy = {},
                )
            }
        }

        composeRule.onNodeWithText("Mixology").assertIsDisplayed()
        composeRule.onNodeWithTag("login_submit").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("login_google").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("login_sign_up").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("login_forgot_password").assertIsDisplayed()
    }

    @Test
    fun loginButton_sendsTrimmedCredentials() {
        var email: String? = null
        var password: String? = null
        composeRule.setContent {
            MixologyTheme {
                LoginScreen(
                    onLogin = { e, p ->
                        email = e
                        password = p
                    },
                    onForgotPassword = {},
                    onSignUp = {},
                    onGoogleSignIn = {},
                    onPrivacyPolicy = {},
                )
            }
        }

        composeRule.onNodeWithText("Username").performTextInput("  user@mixology.test  ")
        composeRule.onNodeWithText("Password").performTextInput("secret")
        composeRule.onNodeWithTag("login_submit").performScrollTo().performClick()

        assertEquals("user@mixology.test", email)
        assertEquals("secret", password)
    }

    @Test
    fun secondaryActions_fireCallbacks() {
        var forgot = false
        var signUp = false
        var google = false
        var privacy = false
        composeRule.setContent {
            MixologyTheme {
                LoginScreen(
                    onLogin = { _, _ -> },
                    onForgotPassword = { forgot = true },
                    onSignUp = { signUp = true },
                    onGoogleSignIn = { google = true },
                    onPrivacyPolicy = { privacy = true },
                )
            }
        }

        composeRule.onNodeWithTag("login_forgot_password").performClick()
        composeRule.onNodeWithTag("login_sign_up").performScrollTo().performClick()
        composeRule.onNodeWithTag("login_google").performScrollTo().performClick()
        composeRule.onNodeWithTag("login_privacy").performScrollTo().performClick()

        assertTrue(forgot)
        assertTrue(signUp)
        assertTrue(google)
        assertTrue(privacy)
    }
}
