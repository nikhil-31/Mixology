package com.capstone.nik.mixology.ui.model

import com.capstone.nik.mixology.Network.CocktailURLs
import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientMeasureTest {

    @Test
    fun ingredientMeasures_skipsBlankSlots_andKeepsPairs() {
        val drink = CocktailDbDrink(
            strIngredient1 = "Tequila",
            strMeasure1 = "1 1/2 oz",
            strIngredient2 = "  Lime juice  ",
            strMeasure2 = "1 oz",
            strIngredient3 = "",
            strMeasure3 = "ignored",
            strIngredient4 = null,
        )

        val measures = drink.ingredientMeasures()

        assertEquals(2, measures.size)
        assertEquals(IngredientMeasure("Tequila", "1 1/2 oz"), measures[0])
        assertEquals(IngredientMeasure("Lime juice", "1 oz"), measures[1])
    }

    @Test
    fun imageUrl_encodesSpaces() {
        val url = IngredientMeasure("Lime juice", "1 oz").imageUrl

        assertTrue(url.startsWith(CocktailURLs.COCKTAIL_INGREDIENTS_URL))
        assertTrue(url.contains("Lime%20juice"))
        assertTrue(url.endsWith(CocktailURLs.COCKTAIL_INGREDIENT_PNG_SMALL))
        assertEquals(url, ingredientImageUrl("Lime juice"))
    }
}
