package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MyRecipesContractTest {

    private val mango = Drink(
        id = "user:1",
        name = "Mango Dragon Fruit Refresher",
        thumb = "",
        ingredients = listOf(IngredientMeasure(ingredient = "Mango juice", measure = "2 oz")),
    )
    private val orange = Drink(
        id = "user:2",
        name = "Orange Coconut Crush",
        thumb = "",
        ingredients = listOf(IngredientMeasure(ingredient = "Coconut cream", measure = "1 oz")),
    )
    private val recipes = listOf(mango, orange)

    @Test
    fun blankQuery_keepsEveryRecipe() {
        assertEquals(recipes, filterRecipes(recipes, "  "))
    }

    @Test
    fun matchesByName_ignoringCaseAndSpaces() {
        assertEquals(listOf(orange), filterRecipes(recipes, "  coconut CRUSH "))
    }

    @Test
    fun matchesByIngredient() {
        assertEquals(listOf(mango), filterRecipes(recipes, "mango juice"))
        assertEquals(listOf(orange), filterRecipes(recipes, "coconut"))
    }

    @Test
    fun noMatch_isReportedOnlyWhenThereAreRecipes() {
        assertTrue(filterRecipes(recipes, "tequila").isEmpty())
        assertTrue(MyRecipesUiState(recipes = recipes, query = "tequila").noMatches)
        assertFalse(MyRecipesUiState(recipes = emptyList(), query = "tequila").noMatches)
        assertEquals(listOf(mango), MyRecipesUiState(recipes = recipes, query = "mango").visible)
    }
}
