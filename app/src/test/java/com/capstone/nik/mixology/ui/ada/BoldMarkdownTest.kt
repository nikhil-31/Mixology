package com.capstone.nik.mixology.ui.ada

import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BoldMarkdownTest {

    @Test
    fun boldRunsLoseTheirAsterisksAndGainWeight() {
        val text = boldMarkdown("Try a **Negroni** or a **Boulevardier**.")

        assertEquals("Try a Negroni or a Boulevardier.", text.text)
        assertEquals(
            listOf("Negroni", "Boulevardier"),
            text.spanStyles.map { text.text.substring(it.start, it.end) },
        )
        assertTrue(text.spanStyles.all { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun aRunStillOpenWhileStreamingIsBoldToTheEnd() {
        val text = boldMarkdown("Added **Gi")

        assertEquals("Added Gi", text.text)
        assertEquals("Gi", text.spanStyles.single().let { text.text.substring(it.start, it.end) })
    }

    @Test
    fun plainTextIsUntouched() {
        assertEquals("2 * 3 = 6", boldMarkdown("2 * 3 = 6").text)
        assertTrue(boldMarkdown("2 * 3 = 6").spanStyles.isEmpty())
    }
}
