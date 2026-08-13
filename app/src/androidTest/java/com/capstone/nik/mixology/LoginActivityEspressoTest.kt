package com.capstone.nik.mixology

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.capstone.nik.mixology.Activities.ActivityLogin
import com.capstone.nik.mixology.Activities.ActivityPasswordChange
import com.capstone.nik.mixology.Activities.ActivitySignUp
import com.capstone.nik.mixology.ui.auth.PRIVACY_POLICY_URL
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityEspressoTest {

    @get:Rule
    val intentsRule = IntentsRule()

    @get:Rule
    val composeRule = createAndroidComposeRule<ActivityLogin>()

    @Test
    fun loginScreen_isVisible_toEspressoAndCompose() {
        Espresso.onView(isRoot()).check(matches(isDisplayed()))
        composeRule.onNodeWithText("Mixology").assertIsDisplayed()
        composeRule.onNodeWithTag("login_submit").assertIsDisplayed()
    }

    @Test
    fun signUpWithEmail_opensSignUpActivity() {
        composeRule.onNodeWithTag("login_sign_up").performScrollTo().performClick()

        intended(hasComponent(ActivitySignUp::class.java.name))
    }

    @Test
    fun forgotPassword_opensPasswordChangeActivity() {
        composeRule.onNodeWithTag("login_forgot_password").performClick()

        intended(hasComponent(ActivityPasswordChange::class.java.name))
    }

    @Test
    fun privacyPolicy_opensBrowserIntent() {
        composeRule.onNodeWithTag("login_privacy").performScrollTo().performClick()

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(Uri.parse(PRIVACY_POLICY_URL)),
            ),
        )
    }
}
