package com.capstone.nik.mixology.ui.search

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MyRecipesSearchTest {

    private val recipes = listOf(
        Drink("u-1", "House Sour", "", ingredients = listOf(IngredientMeasure("Bourbon", "2 oz"))),
        Drink("u-2", "Garden Spritz", "", ingredients = listOf(IngredientMeasure("Elderflower liqueur", "1 oz"))),
    )

    @Test
    fun nameModeMatchesNamesIgnoringCase() {
        assertEquals(listOf("u-1"), filterMyRecipes(recipes, " sour ", SearchMode.NAME).map { it.id })
        assertTrue(filterMyRecipes(recipes, "bourbon", SearchMode.NAME).isEmpty())
    }

    @Test
    fun ingredientModeMatchesIngredients() {
        assertEquals(listOf("u-2"), filterMyRecipes(recipes, "ELDER", SearchMode.INGREDIENT).map { it.id })
        assertTrue(filterMyRecipes(recipes, "sour", SearchMode.INGREDIENT).isEmpty())
    }

    @Test
    fun shortQueriesMatchNothing() {
        assertTrue(filterMyRecipes(recipes, "s", SearchMode.NAME).isEmpty())
    }
}
