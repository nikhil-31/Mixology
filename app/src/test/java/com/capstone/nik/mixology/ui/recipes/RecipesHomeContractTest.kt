package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RecipesHomeContractTest {

    private val drinks = listOf("a", "b", "c").map { Drink(id = it, name = it, thumb = "") }

    @Test
    fun featuredDrink_holdsForADayAndMovesOnTheNext() {
        val today = LocalDate.of(2026, 10, 31)
        assertEquals(featuredDrink(drinks, today), featuredDrink(drinks, today))
        assertNotEquals(featuredDrink(drinks, today), featuredDrink(drinks, today.plusDays(1)))
        assertNull(featuredDrink(emptyList(), today))
    }

    @Test
    fun dayPart_followsTheHour() {
        assertEquals(DayPart.MORNING, dayPart(LocalTime.of(9, 0)))
        assertEquals(DayPart.AFTERNOON, dayPart(LocalTime.of(14, 0)))
        assertEquals(DayPart.EVENING, dayPart(LocalTime.of(19, 0)))
        assertEquals(DayPart.NIGHT, dayPart(LocalTime.of(2, 0)))
    }

    @Test
    fun ingredientSummary_joinsNamesAndSkipsBlanks() {
        val drink = Drink(
            id = "1",
            name = "Margarita",
            thumb = "",
            ingredients = listOf(
                IngredientMeasure("Tequila", "2 oz"),
                IngredientMeasure(" ", ""),
                IngredientMeasure("Lime juice", "1 oz"),
            ),
        )
        assertEquals("Tequila, Lime juice", ingredientSummary(drink))
    }

    @Test
    fun showEmpty_onlyAfterLoadingWithNothingToShow() {
        assertFalse(RecipesHomeUiState().showEmpty)
        assertTrue(RecipesHomeUiState(loaded = true).showEmpty)
        assertFalse(RecipesHomeUiState(loaded = true, saved = drinks).showEmpty)
        assertFalse(RecipesHomeUiState(loaded = true, error = "offline").showEmpty)
    }
}
