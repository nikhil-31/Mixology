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
import com.capstone.nik.mixology.Activities.ActivityDetails
import com.capstone.nik.mixology.Model.Cocktail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ActivityDetailsEspressoTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun missingCocktailExtra_finishes() {
        val scenario = ActivityScenario.launch(ActivityDetails::class.java)
        Espresso.onIdle()
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        scenario.close()
    }

    @Test
    fun cocktailExtra_showsDrinkNameInToolbar() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, ActivityDetails::class.java).putExtra(
            context.getString(R.string.intent_details_intent_cocktail),
            Cocktail("11007", "Margarita", ""),
        )

        ActivityScenario.launch<ActivityDetails>(intent).use { scenario ->
            Espresso.onIdle()
            composeRule.waitForIdle()
            composeRule.onNodeWithText("Margarita").assertIsDisplayed()
            scenario.onActivity { activity ->
                assertTrue(!activity.isFinishing)
            }
        }
    }
}
