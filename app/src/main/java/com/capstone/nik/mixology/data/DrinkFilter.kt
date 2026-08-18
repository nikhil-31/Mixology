package com.capstone.nik.mixology.data

import androidx.annotation.StringRes
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.repository.FilterKind

data class DrinkFilter(
    val name: String,
    val query: String?,
    val kind: FilterKind?,
    @StringRes val titleRes: Int? = null,
    val title: String? = null,
    val showEmptySaved: Boolean = false,
) {
    fun displayName(): String = title ?: name.replace('_', ' ')

    companion object {
        val ALCOHOLIC = DrinkFilter("ALCOHOLIC", "Alcoholic", FilterKind.ALCOHOL, titleRes = R.string.nav_item_alcoholic)
        val NON_ALCOHOLIC = DrinkFilter("NON_ALCOHOLIC", "Non_Alcoholic", FilterKind.ALCOHOL, titleRes = R.string.nav_item_non_alcoholic)
        val COCKTAIL = DrinkFilter("COCKTAIL", "Cocktail", FilterKind.DRINK_TYPE, titleRes = R.string.nav_item_cocktail)
        val ORDINARY_DRINK = DrinkFilter("ORDINARY_DRINK", "Ordinary_Drink", FilterKind.DRINK_TYPE, titleRes = R.string.nav_item_ordinary_drink)
        val GIN = DrinkFilter("GIN", "Gin", FilterKind.INGREDIENT, titleRes = R.string.nav_item_gin)
        val VODKA = DrinkFilter("VODKA", "Vodka", FilterKind.INGREDIENT, titleRes = R.string.nav_item_vodka)
        val RUM = DrinkFilter("RUM", "Rum", FilterKind.INGREDIENT, titleRes = R.string.nav_item_rum)
        val TEQUILA = DrinkFilter("TEQUILA", "Tequila", FilterKind.INGREDIENT, titleRes = R.string.nav_item_tequila)
        val WHISKEY = DrinkFilter("WHISKEY", "Whiskey", FilterKind.INGREDIENT, titleRes = R.string.nav_item_whiskey)
        val VERMOUTH = DrinkFilter("VERMOUTH", "Vermouth", FilterKind.INGREDIENT, titleRes = R.string.nav_item_vermouth)
        val COFFEE_LIQUEUR = DrinkFilter(
            "COFFEE_LIQUEUR",
            "Coffee liqueur",
            FilterKind.INGREDIENT,
            titleRes = R.string.nav_item_coffee_liqueur,
        )
        val BITTERS = DrinkFilter("BITTERS", "Bitters", FilterKind.INGREDIENT, titleRes = R.string.nav_item_bitters)
        val APEROL = DrinkFilter("APEROL", "Aperol", FilterKind.INGREDIENT, titleRes = R.string.nav_item_aperol)
        val COCKTAIL_GLASS = DrinkFilter("COCKTAIL_GLASS", "Cocktail_glass", FilterKind.GLASS, titleRes = R.string.nav_item_cocktail_glass)
        val HIGHBALL_GLASS = DrinkFilter("HIGHBALL_GLASS", "Highball glass", FilterKind.GLASS, titleRes = R.string.nav_item_highball_glass)
        val SAVED = DrinkFilter("SAVED", null, null, titleRes = R.string.nav_item_saved_cocktails, showEmptySaved = true)

        val catalogFilters: List<DrinkFilter> = listOf(
            ALCOHOLIC,
            NON_ALCOHOLIC,
            COCKTAIL,
            ORDINARY_DRINK,
            GIN,
            VODKA,
            COCKTAIL_GLASS,
            HIGHBALL_GLASS,
        )

        val hotFilters: List<DrinkFilter> = listOf(
            VODKA,
            GIN,
            RUM,
            TEQUILA,
            WHISKEY,
            VERMOUTH,
            COFFEE_LIQUEUR,
            BITTERS,
            APEROL,
        )

        val presets: List<DrinkFilter> = (catalogFilters + hotFilters + SAVED).distinct()

        fun dynamic(kind: FilterKind, query: String): DrinkFilter {
            val normalized = query.trim()
            return presets.firstOrNull {
                it.kind == kind && it.query.equals(normalized, ignoreCase = true)
            } ?: DrinkFilter(
                name = "${kind.name}:$normalized",
                query = normalized,
                kind = kind,
                title = normalized.replace('_', ' '),
            )
        }

        fun fromName(name: String): DrinkFilter {
            val decoded = java.net.URLDecoder.decode(name, "UTF-8")
            presets.firstOrNull { it.name == decoded }?.let { return it }
            val separator = decoded.indexOf(':')
            if (separator > 0) {
                val kind = runCatching {
                    FilterKind.valueOf(decoded.substring(0, separator))
                }.getOrNull()
                val query = decoded.substring(separator + 1)
                if (kind != null && query.isNotBlank()) {
                    return dynamic(kind, query)
                }
            }
            return SAVED
        }
    }
}
