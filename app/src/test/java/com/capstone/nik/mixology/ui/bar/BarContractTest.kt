package com.capstone.nik.mixology.ui.bar

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.repository.BarAlmostDrink
import com.capstone.nik.mixology.repository.BarCategory
import com.capstone.nik.mixology.repository.BarSubstitute
import com.capstone.nik.mixology.repository.BarTerm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarContractTest {

    private val catalog = listOf(
        BarTerm("Gin", BarCategory.SPIRIT, 900),
        BarTerm("Sloe gin", BarCategory.LIQUEUR, 30),
        BarTerm("Ginger ale", BarCategory.MIXER, 100),
        BarTerm("Virgin olive oil", BarCategory.OTHER, 500),
        BarTerm("Lime", BarCategory.FRESH, 60),
    )

    @Test
    fun filter_withoutAQuery_putsTheMostUsedFirst() {
        assertEquals(
            listOf("Gin", "Virgin olive oil", "Ginger ale", "Lime", "Sloe gin"),
            filterBarTerms(catalog, "  ").map { it.name },
        )
        assertEquals(listOf("Lime"), filterBarTerms(catalog, "", BarCategory.FRESH).map { it.name })
    }

    @Test
    fun filter_ranksNamesThatStartWithTheQuery_thenWordsThatDo_thenTheRest() {
        assertEquals(
            listOf("Gin", "Ginger ale", "Sloe gin", "Virgin olive oil"),
            filterBarTerms(catalog, "gin").map { it.name },
        )
    }

    @Test
    fun picker_withoutAQuery_listsTheBarThenPopularThenShelves() {
        val state = BarUiState(
            bar = listOf("Lime", "House bitters"),
            catalogTerms = catalog,
            visibleTerms = filterBarTerms(catalog, ""),
        )
        val terms = state.pickerTerms()
        // The bar's own ingredient is listed too, though the catalog doesn't have it.
        assertEquals(listOf("Lime", "House bitters"), terms.selected.map { it.name })
        assertEquals(listOf("Gin", "Virgin olive oil", "Ginger ale", "Sloe gin"), terms.popular.map { it.name })
        assertEquals(
            listOf(BarCategory.SPIRIT, BarCategory.LIQUEUR, BarCategory.MIXER, BarCategory.OTHER),
            terms.sections.map { it.category },
        )
        assertTrue(terms.results.isEmpty())
        assertEquals(null, terms.custom)
    }

    @Test
    fun picker_withThePlainList_isOneAlphabeticalRunWithNoPopular() {
        val plain = listOf(BarTerm("Vodka"), BarTerm("Gin"))
        val terms = BarUiState(catalogTerms = plain, visibleTerms = filterBarTerms(plain, "")).pickerTerms()
        assertTrue(terms.popular.isEmpty())
        assertEquals(listOf(BarPickerSection(null, listOf(BarTerm("Gin"), BarTerm("Vodka")))), terms.sections)
        assertTrue(BarUiState(catalogTerms = plain).pickerCategories().isEmpty())
    }

    @Test
    fun picker_offersAnUnknownQueryAsACustomIngredient_butNotAKnownOne() {
        val base = BarUiState(bar = listOf("House bitters"), catalogTerms = catalog)
        fun searching(query: String) =
            base.copy(query = query, visibleTerms = filterBarTerms(catalog, query)).pickerTerms()

        assertEquals("Tanqueray", searching(" Tanqueray ").custom)
        assertEquals(null, searching("GIN").custom)
        // Already in the bar: listed so it can be unticked, not offered again.
        val own = searching("house bitters")
        assertEquals(null, own.custom)
        assertEquals(listOf("House bitters"), own.results.map { it.name })
    }

    @Test
    fun pickerCategories_areTheOnesTheCatalogHas_inShelfOrder() {
        assertEquals(
            listOf(BarCategory.SPIRIT, BarCategory.LIQUEUR, BarCategory.MIXER, BarCategory.FRESH, BarCategory.OTHER),
            BarUiState(catalogTerms = catalog).pickerCategories(),
        )
    }

    @Test
    fun groupBar_takesTheServersShelf_thenTheCatalogs_thenOther() {
        val groups = groupBar(
            bar = listOf("Campari", "Gin", "Lime", "Tanqueray"),
            categories = mapOf("campari" to BarCategory.LIQUEUR, "tanqueray" to BarCategory.SPIRIT),
            terms = catalog,
        )
        assertEquals(
            listOf(
                BarGroup(BarCategory.SPIRIT, listOf("Gin", "Tanqueray")),
                BarGroup(BarCategory.LIQUEUR, listOf("Campari")),
                BarGroup(BarCategory.FRESH, listOf("Lime")),
            ),
            groups,
        )
        assertEquals(
            listOf(BarGroup(BarCategory.OTHER, listOf("Gin"))),
            groupBar(listOf("Gin"), emptyMap(), emptyList()),
        )
        assertTrue(groupBar(emptyList(), emptyMap(), catalog).isEmpty())
    }

    @Test
    fun almostDrinks_splitIntoSwapsAndShopping() {
        val swap = BarAlmostDrink(
            Drink("1", "Negroni", ""),
            listOf("Campari"),
            listOf(BarSubstitute("campari", "Aperol", "lighter")),
        )
        val half = BarAlmostDrink(
            Drink("2", "Boulevardier", ""),
            listOf("Campari", "Bourbon"),
            listOf(BarSubstitute("Campari", "Aperol", "lighter")),
        )
        val state = BarUiState(loading = false, bar = listOf("Aperol"), almost = listOf(swap, half))
        assertEquals(listOf(swap), state.withSwaps)
        assertEquals(listOf(half), state.needsShopping)
    }

    @Test
    fun noMatches_andFailed_areDifferentThings() {
        val stocked = BarUiState(loading = false, bar = listOf("Absinthe"))
        assertTrue(stocked.noMatches)
        assertEquals(false, stocked.recommendationsFailed)
        val failed = stocked.copy(recommendationsError = com.capstone.nik.mixology.Network.ConnectionError.NO_CONNECTION)
        assertEquals(false, failed.noMatches)
        assertTrue(failed.recommendationsFailed)
    }
}
