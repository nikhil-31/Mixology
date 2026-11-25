package com.capstone.nik.mixology.ui.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogGroupingTest {

    @Test
    fun groupCatalogTermsByLetter_sortsLettersAndBucketsNonLetters() {
        val sections = groupCatalogTermsByLetter(listOf("Gin", "apple", "7-Up", "Vodka", "Aperol"))
        assertEquals(listOf("A", "G", "V", "#"), sections.map { it.letter })
        assertEquals(listOf("apple", "Aperol"), sections.first().terms)
        assertEquals(listOf("7-Up"), sections.last().terms)
    }

    @Test
    fun catalogUsesGrid_onlyForSmallNonEmptySets() {
        assertFalse(catalogUsesGrid(0))
        assertTrue(catalogUsesGrid(12))
        assertFalse(catalogUsesGrid(13))
    }
}
