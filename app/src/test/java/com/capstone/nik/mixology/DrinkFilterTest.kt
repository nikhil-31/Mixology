package com.capstone.nik.mixology

import com.capstone.nik.mixology.Network.remoteModel.CocktailDbDrink
import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkFilterTest {

    @Test
    fun alcoholicFilter_usesAlcoholEndpoint() {
        assertEquals(FilterKind.ALCOHOL, DrinkFilter.ALCOHOLIC.kind)
        assertEquals("Alcoholic", DrinkFilter.ALCOHOLIC.query)
        assertFalse(DrinkFilter.ALCOHOLIC.showEmptySaved)
    }

    @Test
    fun savedFilter_doesNotFetch() {
        assertNull(DrinkFilter.SAVED.kind)
        assertNull(DrinkFilter.SAVED.query)
        assertTrue(DrinkFilter.SAVED.showEmptySaved)
    }

    @Test
    fun hasUsableThumb_rejectsMissingAndLiteralNull() {
        val missing = CocktailDbDrink(strDrinkThumb = null)
        val literalNull = CocktailDbDrink(strDrinkThumb = "null")
        val empty = CocktailDbDrink(strDrinkThumb = "")
        val ok = CocktailDbDrink(strDrinkThumb = "https://example.com/thumb.jpg")

        assertFalse(DrinkRepository.hasUsableThumb(missing))
        assertFalse(DrinkRepository.hasUsableThumb(literalNull))
        assertFalse(DrinkRepository.hasUsableThumb(empty))
        assertTrue(DrinkRepository.hasUsableThumb(ok))
    }
}
