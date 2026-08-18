package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarMatcherTest {

    @Test
    fun inBar_matchesExactAndContainedNames() {
        val bar = setOf("bourbon", "gin")
        assertTrue(BarMatcher.inBar("Gin", bar))
        assertTrue(BarMatcher.inBar("Bourbon whiskey", bar))
        assertTrue(BarMatcher.inBar("bourbon", bar))
        assertEquals(false, BarMatcher.inBar("Campari", bar))
    }

    @Test
    fun missingIngredients_ignoresIceAndWater() {
        val drink = recipe(
            "Gin Fizz",
            "Gin",
            "Lemon juice",
            "Ice",
            "Water",
        )
        assertEquals(listOf("Lemon juice"), BarMatcher.missingIngredients(drink, setOf("gin")))
        assertTrue(BarMatcher.missingIngredients(drink, setOf("gin", "lemon juice")).isEmpty())
    }

    @Test
    fun recommend_splitsMakeableAlmostAndTooManyMissing() {
        val negroni = recipe("Negroni", "Gin", "Campari", "Sweet Vermouth")
        val oldFashioned = recipe("Old Fashioned", "Bourbon", "Sugar", "Bitters")
        val martini = recipe("Martini", "Gin", "Dry Vermouth")
        val result = BarMatcher.recommend(
            listOf(negroni, oldFashioned, martini),
            listOf("Gin", "Campari", "Sweet Vermouth"),
        )
        assertEquals(listOf("Negroni"), result.makeable.map { it.name })
        assertEquals(listOf("Martini"), result.almost.map { it.drink.name })
        assertEquals(listOf("Dry Vermouth"), result.almost.single().missing)
        assertTrue(result.almost.none { it.drink.name == "Old Fashioned" })
    }

    @Test
    fun recommend_emptyBarReturnsNothing() {
        val drink = recipe("Negroni", "Gin", "Campari")
        val result = BarMatcher.recommend(listOf(drink), emptyList())
        assertTrue(result.makeable.isEmpty())
        assertTrue(result.almost.isEmpty())
    }

    @Test
    fun recommend_almostRequiresAtLeastOneSelectedIngredient() {
        val unrelated = recipe("Whiskey Sour", "Bourbon", "Lemon")
        val vodkaShot = recipe("Vodka Shot", "Vodka")
        val related = recipe("Gin Sour", "Gin", "Lemon")
        val result = BarMatcher.recommend(
            listOf(unrelated, vodkaShot, related),
            listOf("Gin"),
        )
        assertEquals(listOf("Gin Sour"), result.almost.map { it.drink.name })
        assertTrue(result.makeable.isEmpty())
    }

    @Test
    fun recommend_ordersAlmostByMissingCountThenName() {
        val b = recipe("B Sour", "Gin", "Lime")
        val a = recipe("A Fizz", "Gin", "Lemon", "Soda")
        val result = BarMatcher.recommend(listOf(a, b), listOf("Gin"))
        assertEquals(listOf("B Sour", "A Fizz"), result.almost.map { it.drink.name })
    }

    private fun recipe(name: String, vararg ingredients: String): Drink = Drink(
        id = name,
        name = name,
        thumb = "",
        instructions = "Mix.",
        ingredients = ingredients.map { IngredientMeasure(it, "1 oz") },
    )
}
