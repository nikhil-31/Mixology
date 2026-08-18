package com.capstone.nik.mixology

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.capstone.nik.mixology.Activities.ActivityMain
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.putDrinkExtra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ActivityMainEspressoTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun launchWithoutExtras_staysResumed() {
        ActivityScenario.launch(ActivityMain::class.java).use { scenario ->
            Espresso.onIdle()
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test
    fun drinkExtras_showsDrinkName() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, ActivityMain::class.java).putDrinkExtra(
            Drink("11007", "Margarita", ""),
        )

        ActivityScenario.launch<ActivityMain>(intent).use { scenario ->
            Espresso.onIdle()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Margarita").assertIsDisplayed()
            scenario.onActivity { activity ->
                assertTrue(!activity.isFinishing)
            }
        }
    }
}
