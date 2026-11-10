package com.capstone.nik.mixology.repository

import com.capstone.nik.mixology.Network.remoteModel.ApiBarAlmost
import com.capstone.nik.mixology.Network.remoteModel.ApiBarBuyNext
import com.capstone.nik.mixology.Network.remoteModel.ApiBarItem
import com.capstone.nik.mixology.Network.remoteModel.ApiBarSubstitute
import com.capstone.nik.mixology.Network.remoteModel.ApiDrink
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
    fun inBar_matchesWholeWordsOnly_asTheServerDoes() {
        val bar = setOf("gin", "lemon")
        assertTrue(BarMatcher.inBar("London dry gin", bar))
        assertTrue(BarMatcher.inBar("Lemon juice", bar))
        assertTrue(BarMatcher.inBar("Lemon-lime soda", bar))
        assertEquals(false, BarMatcher.inBar("Ginger ale", bar))
        assertEquals(false, BarMatcher.inBar("Virgin olive oil", bar))
        // Either way round: a bar with "Sloe gin" has what a recipe calls "Gin".
        assertTrue(BarMatcher.inBar("Gin", setOf("sloe gin")))
        assertEquals(false, BarMatcher.inBar("Gin", setOf("ginger ale")))
    }

    @Test
    fun recommendations_preferTheServersBuyNext_andFallBackToTheirOwn() {
        val almost = listOf(BarAlmostDrink(Drink("1", "Martini", ""), listOf("Dry Vermouth")))
        assertEquals(listOf(BarUnlock("Dry Vermouth", 1)), BarRecommendations(almost = almost).unlocks)
        val server = listOf(BarUnlock("Mint", 0, nearly = 4))
        assertEquals(server, BarRecommendations(almost = almost, buyNext = server).unlocks)
        assertTrue(BarRecommendations(almost = almost, buyNext = emptyList()).unlocks.isEmpty())
    }

    @Test
    fun barRecommendationsOf_mapsSubstitutesShelvesAndBuyNext() {
        val recs = barRecommendationsOf(
            makeable = emptyList(),
            almost = listOf(
                ApiBarAlmost(
                    ApiDrink(id = "1", name = "Negroni", thumb = "https://example.com/n.jpg"),
                    listOf("Campari"),
                    listOf(ApiBarSubstitute("Campari", "Aperol", "lighter"), ApiBarSubstitute("", "", "")),
                ),
            ),
            items = listOf(ApiBarItem("Gin", "spirit"), ApiBarItem("Odd", "new shelf")),
            buyNext = listOf(ApiBarBuyNext("Campari", unlocks = 1, nearly = 2)),
        )
        assertEquals(listOf(BarSubstitute("Campari", "Aperol", "lighter")), recs.almost.single().substitutes)
        assertTrue(recs.almost.single().makeableWithSwaps)
        assertEquals(mapOf("gin" to BarCategory.SPIRIT, "odd" to BarCategory.OTHER), recs.categories)
        assertEquals(listOf(BarUnlock("Campari", 1, 2)), recs.unlocks)
        // An older server sends none of it.
        val old = barRecommendationsOf(emptyList(), listOf(ApiBarAlmost(ApiDrink(id = "1", name = "N", thumb = "t"), listOf("X"))))
        assertEquals(null, old.buyNext)
        assertTrue(old.almost.single().substitutes.isEmpty())
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

    @Test
    fun topUnlocks_ranksSingleMissingIngredients() {
        fun almost(id: String, vararg missing: String) =
            BarAlmostDrink(recipe(id), missing.toList())
        val recs = BarRecommendations(
            almost = listOf(
                almost("a", "Lime Juice"),
                almost("b", "lime  juice"),
                almost("c", "Campari"),
                almost("d", "Ice"),
                almost("e", "Bitters", "Sugar"),
                almost("f", "Aperol"),
            ),
        )
        assertEquals(
            listOf(BarUnlock("Lime Juice", 2), BarUnlock("Aperol", 1)),
            recs.topUnlocks(limit = 2),
        )
        assertTrue(BarRecommendations().topUnlocks().isEmpty())
    }

    private fun recipe(name: String, vararg ingredients: String): Drink = Drink(
        id = name,
        name = name,
        thumb = "",
        instructions = "Mix.",
        ingredients = ingredients.map { IngredientMeasure(it, "1 oz") },
    )
}
