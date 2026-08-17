package com.capstone.nik.mixology.ui

import android.app.Application
import android.content.Intent
import com.capstone.nik.mixology.Model.Cocktail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class IntentExtrasTest {

    @Test
    fun drinkExtra_returnsIdNameAndThumb() {
        val cocktail = Cocktail("11007", "Margarita", "https://example.com/m.jpg")
        val intent = Intent().putDrinkExtra(cocktail)

        val restored = intent.drinkExtra()

        assertEquals("11007", restored?.getmDrinkId())
        assertEquals("Margarita", restored?.getmDrinkName())
        assertEquals("https://example.com/m.jpg", restored?.getmDrinkThumb())
    }

    @Test
    fun drinkExtra_returnsNullWhenIdMissing() {
        assertNull(Intent().drinkExtra())
        assertNull(Intent().putExtra(DrinkIntents.EXTRA_ID, " ").drinkExtra())
    }
}
