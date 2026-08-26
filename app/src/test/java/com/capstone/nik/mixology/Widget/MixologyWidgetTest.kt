package com.capstone.nik.mixology.Widget

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.capstone.nik.mixology.Activities.ActivityMain
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.DrinkIntents
import com.capstone.nik.mixology.ui.drinkExtra
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MixologyWidgetTest {

    @Test
    fun openAppIntent_targetsMainActivity() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val intent = openAppIntent(context)
        assertEquals(ActivityMain::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP != 0)
    }

    @Test
    fun openDrinkIntent_putsDrinkExtras() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val drink = Drink("11007", "Margarita", "https://example.com/m.jpg")
        val intent = openDrinkIntent(context, drink)

        assertEquals(DrinkIntents.ACTION_OPEN_DRINK, intent.action)
        assertEquals("11007", intent.drinkExtra()?.id)
        assertEquals("Margarita", intent.drinkExtra()?.name)
        assertEquals("https://example.com/m.jpg", intent.drinkExtra()?.thumb)
    }
}
