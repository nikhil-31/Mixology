package com.capstone.nik.mixology.ui.details

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ShareTextTest {

    private fun share(drink: Drink) = shareText(drink, "Ingredients", "Instructions", "Shared from Mixology")

    @Test
    fun aFullRecipeReadsAsNameKindIngredientsAndSteps() {
        val drink = Drink(
            id = "11007",
            name = "Margarita",
            thumb = "",
            alcoholic = "Alcoholic",
            glass = "Cocktail glass",
            instructions = "Shake with ice. Strain.",
            ingredients = listOf(IngredientMeasure("Tequila", "1 1/2 oz"), IngredientMeasure("Salt", "")),
        )

        assertEquals(
            """
            Margarita
            Alcoholic · Cocktail glass

            Ingredients
            • 1 1/2 oz Tequila
            • Salt

            Instructions
            Shake with ice. Strain.

            Shared from Mixology
            """.trimIndent(),
            share(drink),
        )
    }

    @Test
    fun whatARecipeLacksIsLeftOut() {
        val text = share(Drink(id = "recipe-1", name = "House punch", thumb = ""))

        assertEquals("House punch\n\nShared from Mixology", text)
        assertFalse(text.contains("null"))
    }
}
