package com.capstone.nik.mixology.data

import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientListConverterTest {

    private val converter = IngredientListConverter()

    @Test
    fun roundTrip_preservesIngredients() {
        val ingredients = listOf(
            IngredientMeasure("Gin", "1 oz"),
            IngredientMeasure("Campari", "1 oz"),
        )
        val json = converter.toJson(ingredients)
        assertEquals(ingredients, converter.fromJson(json))
    }

    @Test
    fun fromJson_blankOrNull_returnsEmpty() {
        assertTrue(converter.fromJson(null).isEmpty())
        assertTrue(converter.fromJson("").isEmpty())
        assertTrue(converter.fromJson("   ").isEmpty())
    }

    @Test
    fun toJson_emptyOrNull_returnsNull() {
        assertNull(converter.toJson(null))
        assertNull(converter.toJson(emptyList()))
    }
}
