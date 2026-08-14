package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.DrinkFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerDestinationTest {

    @Test
    fun gridRoute_usesFilterName() {
        assertEquals("grid/ALCOHOLIC", gridRoute(DrinkFilter.ALCOHOLIC))
        assertEquals("grid/SAVED", DrawerDestination.Filter(DrinkFilter.SAVED).route)
        assertEquals("randomixer", DrawerDestination.Randomixer.route)
        assertEquals("hot", DrawerDestination.Hot.route)
        assertEquals("settings", DrawerDestination.Settings.route)
    }

    @Test
    fun drawerSections_includeSavedRandomixerAndFilters() {
        val destinations = drawerSections.flatMap { it.items }.map { it.destination }
        assertTrue(destinations.any { it is DrawerDestination.Hot })
        assertTrue(destinations.any { it is DrawerDestination.Randomixer })
        assertTrue(destinations.any { it is DrawerDestination.Filter && it.filter == DrinkFilter.GIN })
        assertTrue(destinations.any { it is DrawerDestination.Filter && it.filter == DrinkFilter.SAVED })
    }

    @Test
    fun showsSideNav_onlyOnHomeBrowseScreens() {
        assertFalse(DrawerDestination.Hot.showsSideNav())
        assertTrue(DrawerDestination.Filter(DrinkFilter.ALCOHOLIC).showsSideNav())
        assertTrue(DrawerDestination.Filter(DrinkFilter.GIN).showsSideNav())
        assertFalse(DrawerDestination.Filter(DrinkFilter.SAVED).showsSideNav())
        assertFalse(DrawerDestination.Randomixer.showsSideNav())
        assertFalse(DrawerDestination.Settings.showsSideNav())
    }
}
