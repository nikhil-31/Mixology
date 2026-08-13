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
    fun getCocktailExtra_returnsParcelledCocktail() {
        val cocktail = Cocktail("11007", "Margarita", "https://example.com/m.jpg")
        val intent = Intent().putExtra("Cocktail", cocktail)

        val restored = intent.getCocktailExtra("Cocktail")

        assertEquals("11007", restored?.getmDrinkId())
        assertEquals("Margarita", restored?.getmDrinkName())
        assertEquals("https://example.com/m.jpg", restored?.getmDrinkThumb())
    }

    @Test
    fun getCocktailExtra_returnsNullWhenMissing() {
        assertNull(Intent().getCocktailExtra("Cocktail"))
    }
}
