package com.capstone.nik.mixology.Activities

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ActivityDetailsRobolectricTest {

    @Test
    fun missingCocktailExtra_destroysActivity() {
        val scenario = ActivityScenario.launch(ActivityDetails::class.java)
        assertEquals(Lifecycle.State.DESTROYED, scenario.state)
        scenario.close()
    }
}
