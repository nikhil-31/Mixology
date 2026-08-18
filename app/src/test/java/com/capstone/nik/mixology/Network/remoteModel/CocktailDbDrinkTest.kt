package com.capstone.nik.mixology.Network.remoteModel

import org.junit.Assert.assertEquals
import org.junit.Test

class CocktailDbDrinkTest {

    @Test
    fun localizedInstructions_prefersSpanishWhenPresent() {
        val drink = CocktailDbDrink(
            strInstructions = "Shake with ice.",
            strInstructionsES = "Agitar con hielo.",
        )
        assertEquals("Agitar con hielo.", drink.localizedInstructions("es"))
        assertEquals("Shake with ice.", drink.localizedInstructions("en"))
    }

    @Test
    fun toDrink_usesLocalizedInstructions() {
        val original = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale("es"))
            val drink = CocktailDbDrink(
                idDrink = "1",
                strDrink = "Margarita",
                strDrinkThumb = "https://example.com/a.jpg",
                strInstructions = "Shake.",
                strInstructionsES = "Agitar.",
            ).toDrink()
            assertEquals("Agitar.", drink?.instructions)
        } finally {
            java.util.Locale.setDefault(original)
        }
    }
}
