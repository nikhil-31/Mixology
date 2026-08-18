package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerDestinationTest {

    @Test
    fun gridRoute_usesFilterName() {
        assertEquals("grid/ALCOHOLIC", gridRoute(DrinkFilter.ALCOHOLIC))
        assertEquals(
            "grid/INGREDIENT%3ATequila",
            gridRoute(DrinkFilter.dynamic(com.capstone.nik.mixology.repository.FilterKind.INGREDIENT, "Tequila")),
        )
        assertEquals("grid/SAVED", DrawerDestination.Filter(DrinkFilter.SAVED).route)
        assertEquals("randomixer", DrawerDestination.Randomixer.route)
        assertEquals("hot", DrawerDestination.Hot.route)
        assertEquals("settings", DrawerDestination.Settings.route)
        assertEquals("catalog", DrawerDestination.Catalog.route)
        assertEquals("shopping", DrawerDestination.Shopping.route)
        assertEquals("search?query=gin", searchRoute("gin"))
        assertEquals(
            "details/11007?name=Margarita&thumb=https%3A%2F%2Fexample.com%2Fa.jpg",
            detailsRoute(Drink("11007", "Margarita", "https://example.com/a.jpg")),
        )
        assertTrue(isOverlayRoute("search?query={query}"))
        assertTrue(isOverlayRoute("details/{id}?name={name}&thumb={thumb}"))
        assertFalse(isOverlayRoute("hot"))
    }

    @Test
    fun bottomNavTab_excludesCatalogShoppingAndFilterGrids() {
        assertTrue(DrawerDestination.Hot.isBottomNavTab())
        assertTrue(DrawerDestination.Filter(DrinkFilter.SAVED).isBottomNavTab())
        assertTrue(DrawerDestination.Randomixer.isBottomNavTab())
        assertTrue(DrawerDestination.Settings.isBottomNavTab())
        assertFalse(DrawerDestination.Catalog.isBottomNavTab())
        assertFalse(DrawerDestination.Shopping.isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.GIN).isBottomNavTab())
        assertFalse(DrawerDestination.Filter(DrinkFilter.ALCOHOLIC).isBottomNavTab())
    }
}
