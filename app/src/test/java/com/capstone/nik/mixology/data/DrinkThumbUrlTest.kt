package com.capstone.nik.mixology.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkThumbUrlTest {

    private val base = "https://api.mixology.test/"

    @Test
    fun relativeRecipePhotosResolveAgainstTheApi() {
        assertEquals(
            "https://api.mixology.test/recipe-photos/abc.jpg",
            Drink.resolveThumbUrl("recipe-photos/abc.jpg", base),
        )
        assertEquals(
            "https://api.mixology.test/recipe-photos/abc.jpg",
            Drink.resolveThumbUrl("/recipe-photos/abc.jpg", "https://api.mixology.test"),
        )
    }

    @Test
    fun absoluteThumbsPassThrough() {
        val catalog = "https://www.thecocktaildb.com/images/media/drink/margarita.jpg"
        assertEquals(catalog, Drink.resolveThumbUrl(catalog, base))
        assertEquals("http://10.0.2.2:8000/a.jpg", Drink.resolveThumbUrl("http://10.0.2.2:8000/a.jpg", base))
    }

    @Test
    fun missingThumbsHaveNoUrl() {
        assertNull(Drink.resolveThumbUrl(null, base))
        assertNull(Drink.resolveThumbUrl("", base))
        assertNull(Drink.resolveThumbUrl("null", base))
        assertNull(Drink("u-1", "Mine", "").thumbUrl)
    }

    @Test
    fun userRecipesAreTheUPrefixedIds() {
        assertTrue(Drink("u-abc", "Mine", "").isUserRecipe)
        assertFalse(Drink("11007", "Margarita", "").isUserRecipe)
    }
}
