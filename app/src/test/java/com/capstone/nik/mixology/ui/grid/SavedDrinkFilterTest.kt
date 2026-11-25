package com.capstone.nik.mixology.ui.grid

import com.capstone.nik.mixology.data.Drink
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedDrinkFilterTest {

    @Test
    fun filterSavedDrinks_matchesNameIgnoringCase() {
        val drinks = listOf(
            Drink("1", "Margarita", ""),
            Drink("2", "Mojito", ""),
        )
        assertEquals(listOf("Margarita"), filterSavedDrinks(drinks, "mar").map { it.name })
        assertEquals(drinks.map { it.name }, filterSavedDrinks(drinks, "  ").map { it.name })
    }
}
