package com.capstone.nik.mixology.ui.tour

import android.app.Application
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class WelcomeTourPreferencesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Application

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("mixology", 0).edit().clear().commit()
    }

    @Test
    fun unseenUntilMarked_andCanBeClearedForAReplay() {
        assertFalse(WelcomeTourPreferences.isSeen(context))

        WelcomeTourPreferences.setSeen(context, true)
        assertTrue(WelcomeTourPreferences.isSeen(context))
        assertTrue(context.getSharedPreferences("mixology", 0).getBoolean("welcome_tour_seen", false))

        WelcomeTourPreferences.setSeen(context, false)
        assertFalse(WelcomeTourPreferences.isSeen(context))
    }

    @Test
    fun rememberWelcomeTourSeen_followsTheFlag() {
        composeRule.setContent {
            Text(
                text = rememberWelcomeTourSeen().toString(),
                modifier = Modifier.testTag("seen"),
            )
        }
        composeRule.onNodeWithTag("seen").assertTextEquals("false")

        composeRule.runOnUiThread { WelcomeTourPreferences.setSeen(context, true) }
        composeRule.onNodeWithTag("seen").assertTextEquals("true")

        composeRule.runOnUiThread { WelcomeTourPreferences.setSeen(context, false) }
        composeRule.onNodeWithTag("seen").assertTextEquals("false")
    }
}
