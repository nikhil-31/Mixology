package com.capstone.nik.mixology.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrinkRecipeBodyTest {

    @Test
    fun instructionSteps_blank_returnsEmpty() {
        assertTrue(instructionSteps(null).isEmpty())
        assertTrue(instructionSteps("  ").isEmpty())
    }

    @Test
    fun instructionSteps_singleParagraph_staysIntact() {
        assertEquals(
            listOf("Shake and strain."),
            instructionSteps("Shake and strain."),
        )
    }

    @Test
    fun instructionSteps_numbered_splitsOnIndexes() {
        assertEquals(
            listOf("Shake with ice.", "Strain into the glass."),
            instructionSteps("1. Shake with ice. 2. Strain into the glass."),
        )
    }

    @Test
    fun instructionSteps_newlines_becomeSteps() {
        assertEquals(
            listOf("Shake with ice.", "Strain into the glass."),
            instructionSteps("Shake with ice.\nStrain into the glass."),
        )
    }

    @Test
    fun instructionSteps_paragraphOfSentences_becomesSteps() {
        assertEquals(
            listOf(
                "Rub the rim of the glass with lime.",
                "Shake the other ingredients with ice.",
                "Strain into the glass.",
            ),
            instructionSteps(
                "Rub the rim of the glass with lime. Shake the other ingredients with ice. Strain into the glass.",
            ),
        )
    }

    @Test
    fun instructionSteps_shortSentences_stayOneBlock() {
        assertEquals(listOf("Stir. Serve."), instructionSteps("Stir. Serve."))
    }

    @Test
    fun instructionSteps_abbreviationsWithoutCapitals_dontSplit() {
        val text = "Add 2 oz. of gin and stir well with ice cubes."
        assertEquals(listOf(text), instructionSteps(text))
    }
}
