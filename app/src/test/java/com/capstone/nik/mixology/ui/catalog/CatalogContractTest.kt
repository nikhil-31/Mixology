package com.capstone.nik.mixology.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogContractTest {

    @Test
    fun filterCatalogTerms_matchesCaseInsensitively() {
        val terms = listOf("Tequila", "Rum", "Vodka")

        assertEquals(listOf("Rum"), filterCatalogTerms(terms, "rum"))
        assertEquals(listOf("Vodka"), filterCatalogTerms(terms, "VOD"))
        assertEquals(terms, filterCatalogTerms(terms, ""))
        assertEquals(terms, filterCatalogTerms(terms, "   "))
    }
}
