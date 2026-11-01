package com.capstone.nik.mixology.ui.shopping

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.ShoppingItemEntity
import com.capstone.nik.mixology.data.ShoppingSection
import com.capstone.nik.mixology.ui.model.IngredientMeasure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListTest {

    private val margarita = Drink(
        "1", "Margarita", "",
        ingredients = listOf(IngredientMeasure("Tequila", "2 oz"), IngredientMeasure("Lime juice", "1 oz")),
    )
    private val daiquiri = Drink("2", "Daiquiri", "", ingredients = listOf(IngredientMeasure("Lime  Juice", "3/4 oz")))

    private fun sections(limeChecked: Boolean = false, daiquiriChecked: Boolean = false) = listOf(
        ShoppingSection(
            "1", margarita,
            listOf(
                ShoppingItemEntity(id = 1, name = "Tequila", drinkId = "1"),
                ShoppingItemEntity(id = 2, name = "Lime juice", checked = limeChecked, drinkId = "1"),
            ),
        ),
        ShoppingSection("2", daiquiri, listOf(ShoppingItemEntity(id = 3, name = "Lime  Juice", checked = daiquiriChecked, drinkId = "2"))),
    )

    @Test
    fun sortCheckedLast_keepsOrderWithinEachGroup() {
        val items = listOf(
            ShoppingItemEntity(id = 1, name = "a", checked = true),
            ShoppingItemEntity(id = 2, name = "b"),
            ShoppingItemEntity(id = 3, name = "c", checked = true),
            ShoppingItemEntity(id = 4, name = "d"),
        )
        assertEquals(listOf(2L, 4L, 1L, 3L), sortCheckedLast(items).map { it.id })
    }

    @Test
    fun combine_mergesIgnoringCaseAndSpacing_andListsDrinks() {
        val combined = combineByIngredient(sections())
        assertEquals(listOf("Lime juice", "Tequila"), combined.map { it.name })
        val lime = combined.first()
        assertEquals(listOf(2L, 3L), lime.items.map { it.id })
        assertEquals(listOf("Margarita", "Daiquiri"), lime.drinkNames)
        // Two drinks need different amounts, so no single measure is shown.
        assertEquals("", lime.measure)
        assertEquals("2 oz", combined.last().measure)
    }

    @Test
    fun combinedRow_isCheckedOnlyWhenEveryItemIs_andSinks() {
        val partly = combineByIngredient(sections(limeChecked = true))
        assertFalse(partly.first { it.name == "Lime juice" }.checked)

        val fully = combineByIngredient(sections(limeChecked = true, daiquiriChecked = true))
        assertTrue(fully.first { it.name == "Lime juice" }.checked)
        assertEquals("Tequila", fully.first().name)
    }

    @Test
    fun shareText_byDrink_skipsCheckedAndGroups() {
        val text = shoppingShareText(sections(limeChecked = true), byIngredient = false, otherLabel = "Other")
        assertEquals("Margarita\n☐ Tequila (2 oz)\n\nDaiquiri\n☐ Lime  Juice (3/4 oz)", text)
    }

    @Test
    fun shareText_byIngredient_isFlat_andNullWhenAllDone() {
        assertEquals(
            "☐ Lime juice\n☐ Tequila (2 oz)",
            shoppingShareText(sections(), byIngredient = true, otherLabel = "Other"),
        )
        val done = sections(limeChecked = true, daiquiriChecked = true).map { s ->
            s.copy(items = s.items.map { it.copy(checked = true) })
        }
        assertNull(shoppingShareText(done, byIngredient = false, otherLabel = "Other"))
    }

    // My recipes aren't in the catalog cache, so the repository leaves their sections without a drink.
    private val mySour = Drink(
        "u-1", "Aaa Sour", "",
        ingredients = listOf(IngredientMeasure("Lemon", "1 oz"), IngredientMeasure("Honey", "1/2 oz")),
    )
    private fun withRecipeAndOther() = listOf(
        ShoppingSection("1", margarita, listOf(ShoppingItemEntity(id = 1, name = "Tequila", drinkId = "1"))),
        ShoppingSection(
            "u-1", null,
            listOf(
                ShoppingItemEntity(id = 5, name = "Honey", drinkId = "u-1"),
                ShoppingItemEntity(id = 4, name = "Lemon", drinkId = "u-1"),
            ),
        ),
        ShoppingSection("", null, listOf(ShoppingItemEntity(id = 9, name = "Coffee"))),
    )

    @Test
    fun withRecipes_fillsMyRecipe_sortsByItsName_andKeepsOtherLast() {
        val sections = com.capstone.nik.mixology.data.withRecipes(withRecipeAndOther(), listOf(mySour))
        assertEquals(listOf("u-1", "1", ""), sections.map { it.drinkId })
        val recipe = sections.first()
        assertEquals("Aaa Sour", recipe.drink?.name)
        // Recipe order, with the recipe's measures.
        assertEquals(listOf("Lemon", "Honey"), recipe.items.map { it.name })
        assertEquals("1 oz", recipe.measure(recipe.items.first()))
        assertNull(sections.last().drink)
    }

    @Test
    fun withRecipes_deletedRecipeStaysDrinkless_andNothingToFillReturnsSameList() {
        val deleted = com.capstone.nik.mixology.data.withRecipes(withRecipeAndOther(), listOf(daiquiri))
        assertNull(deleted.first { it.drinkId == "u-1" }.drink)

        val plain = sections()
        assertTrue(plain === com.capstone.nik.mixology.data.withRecipes(plain, listOf(mySour)))
    }
}
