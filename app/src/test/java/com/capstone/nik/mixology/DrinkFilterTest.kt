package com.capstone.nik.mixology

import com.capstone.nik.mixology.data.DrinkFilter
import com.capstone.nik.mixology.repository.DrinkRepository
import com.capstone.nik.mixology.repository.FilterKind
import com.capstone.nik.mixology.Network.remoteModel.Drink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkFilterTest {

    @Test
    fun fromNavId_mapsDrawerDestinations() {
        assertEquals(DrinkFilter.ALCOHOLIC, DrinkFilter.fromNavId(R.id.nav_Alcoholic))
        assertEquals(DrinkFilter.NON_ALCOHOLIC, DrinkFilter.fromNavId(R.id.nav_Non_Alcoholic))
        assertEquals(DrinkFilter.GIN, DrinkFilter.fromNavId(R.id.nav_gin))
        assertEquals(DrinkFilter.SAVED, DrinkFilter.fromNavId(R.id.Saved_Cocktails))
        assertNull(DrinkFilter.fromNavId(R.id.nav_randomixer))
    }

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
        val missing = Drink().apply { strDrinkThumb = null }
        val literalNull = Drink().apply { strDrinkThumb = "null" }
        val empty = Drink().apply { strDrinkThumb = "" }
        val ok = Drink().apply { strDrinkThumb = "https://example.com/thumb.jpg" }

        assertFalse(DrinkRepository.hasUsableThumb(missing))
        assertFalse(DrinkRepository.hasUsableThumb(literalNull))
        assertFalse(DrinkRepository.hasUsableThumb(empty))
        assertTrue(DrinkRepository.hasUsableThumb(ok))
    }
}
