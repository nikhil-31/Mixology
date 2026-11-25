package com.capstone.nik.mixology.ui.recipes

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarRecommendations
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

    private fun drinks(prefix: String, count: Int) =
        (1..count).map { Drink(id = "$prefix$it", name = "$prefix$it", thumb = "") }

    private fun recs(makeable: Int, almost: Int) = BarRecommendations(
        makeable = drinks("m", makeable),
        almost = drinks("a", almost).map { BarAlmostDrink(it, listOf("Lime", "Mint", "Soda")) },
    )

    @Test
    fun heroSlides_capsAtEightMakeableThenTwoAlmost() {
        val slides = heroSlides(recs(makeable = 12, almost = 5), LocalDate.of(2026, 11, 1))
        assertEquals(HERO_MAKEABLE_LIMIT + HERO_ALMOST_LIMIT, slides.size)
        assertTrue(slides.take(8).all { it is HeroSlide.CanMake })
        assertTrue(slides.drop(8).all { it is HeroSlide.Almost })
        assertEquals(slides.size, slides.map { it.drink.id }.distinct().size)
    }

    @Test
    fun heroSlides_holdForADayAndStartElsewhereTheNext() {
        val recs = recs(makeable = 5, almost = 3)
        val today = LocalDate.of(2026, 11, 1)
        assertEquals(heroSlides(recs, today), heroSlides(recs, today))
        assertNotEquals(heroSlides(recs, today).first(), heroSlides(recs, today.plusDays(1)).first())
    }

    @Test
    fun heroSlides_almostCarriesWhatIsMissing() {
        val slide = heroSlides(recs(makeable = 0, almost = 1)).single() as HeroSlide.Almost
        assertEquals(listOf("Lime", "Mint", "Soda"), slide.missing)
        assertEquals("Lime, Mint", missingSummary(slide.missing))
    }

    @Test
    fun heroSlides_ownRecipesStayFirstWhileOthersRotate() {
        val own = drinks("u-", 2)
        val recs = BarRecommendations(
            makeable = own + drinks("m", 5),
            almost = drinks("u-a", 1).plus(drinks("a", 3)).map { BarAlmostDrink(it, listOf("Lime")) },
        )
        val start = LocalDate.of(2026, 11, 1)
        val firstOthers = mutableSetOf<String>()
        (0L until 5L).forEach { offset ->
            val slides = heroSlides(recs, start.plusDays(offset))
            val makeable = slides.filterIsInstance<HeroSlide.CanMake>()
            val almost = slides.filterIsInstance<HeroSlide.Almost>()
            assertEquals(listOf("u-1", "u-2"), makeable.take(2).map { it.drink.id })
            assertEquals("u-a1", almost.first().drink.id)
            assertEquals(7, makeable.size)
            firstOthers += makeable[2].drink.id
        }
        assertEquals(setOf("m1", "m2", "m3", "m4", "m5"), firstOthers)
    }

    @Test
    fun heroSlides_ownRecipesCountTowardTheCaps() {
        val recs = BarRecommendations(makeable = drinks("u-", 10) + drinks("m", 3))
        val slides = heroSlides(recs, LocalDate.of(2026, 11, 1))
        assertEquals((1..HERO_MAKEABLE_LIMIT).map { "u-$it" }, slides.map { it.drink.id })
    }

    @Test
    fun heroSlides_emptyWithoutRecommendations() {
        assertTrue(heroSlides(BarRecommendations()).isEmpty())
    }
}
