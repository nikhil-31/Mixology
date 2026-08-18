package com.capstone.nik.mixology.ui.main

import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.data.DrinkFilter
import java.net.URLEncoder

sealed class DrawerDestination(val route: String) {
    data class Filter(val filter: DrinkFilter) : DrawerDestination(gridRoute(filter))
    data object Randomixer : DrawerDestination("randomixer")
    data object Hot : DrawerDestination("hot")
    data object Settings : DrawerDestination("settings")
    data object Catalog : DrawerDestination("catalog")
    data object Shopping : DrawerDestination("shopping")
}

const val GRID_ROUTE = "grid/{filter}"
const val RANDOMIXER_ROUTE = "randomixer"
const val HOT_ROUTE = "hot"
const val SETTINGS_ROUTE = "settings"
const val CATALOG_ROUTE = "catalog"
const val SHOPPING_ROUTE = "shopping"
const val SEARCH_ROUTE = "search?query={query}"
const val DETAILS_ROUTE = "details/{id}?name={name}&thumb={thumb}"

fun gridRoute(filter: DrinkFilter) = "grid/${encodeRouteArg(filter.name)}"

fun searchRoute(query: String = "") = "search?query=${encodeRouteArg(query)}"

fun detailsRoute(drink: Drink): String {
    val id = encodeRouteArg(drink.id)
    val name = encodeRouteArg(drink.name)
    val thumb = encodeRouteArg(drink.thumb)
    return "details/$id?name=$name&thumb=$thumb"
}

fun isOverlayRoute(route: String?): Boolean {
    if (route == null) return false
    return route.startsWith("search") || route.startsWith("details")
}

private fun encodeRouteArg(value: String): String {
    return URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
