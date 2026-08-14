package com.capstone.nik.mixology.data

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.repository.FilterKind

enum class DrinkFilter(
    val query: String?,
    val kind: FilterKind?,
    @StringRes val titleRes: Int,
    val showEmptySaved: Boolean = false,
) {
    ALCOHOLIC("Alcoholic", FilterKind.ALCOHOL, R.string.nav_item_alcoholic),
    NON_ALCOHOLIC("Non_Alcoholic", FilterKind.ALCOHOL, R.string.nav_item_non_alcoholic),
    COCKTAIL("Cocktail", FilterKind.DRINK_TYPE, R.string.nav_item_cocktail),
    ORDINARY_DRINK("Ordinary_Drink", FilterKind.DRINK_TYPE, R.string.nav_item_ordinary_drink),
    GIN("Gin", FilterKind.INGREDIENT, R.string.nav_item_gin),
    VODKA("Vodka", FilterKind.INGREDIENT, R.string.nav_item_vodka),
    COCKTAIL_GLASS("Cocktail_glass", FilterKind.GLASS, R.string.nav_item_cocktail_glass),
    HIGHBALL_GLASS("Highball glass", FilterKind.GLASS, R.string.nav_item_highball_glass),
    SAVED(null, null, R.string.nav_item_saved_cocktails, showEmptySaved = true);

    companion object {
        val catalogFilters: List<DrinkFilter> get() = entries.filter { it.kind != null }

        @JvmStatic
        fun fromNavId(@IdRes id: Int): DrinkFilter? = when (id) {
            R.id.nav_Alcoholic -> ALCOHOLIC
            R.id.nav_Non_Alcoholic -> NON_ALCOHOLIC
            R.id.nav_Cocktail -> COCKTAIL
            R.id.nav_Ordinary_Drink -> ORDINARY_DRINK
            R.id.nav_gin -> GIN
            R.id.nav_vodka -> VODKA
            R.id.nav_cocktail_glass -> COCKTAIL_GLASS
            R.id.nav_Highball_Glass -> HIGHBALL_GLASS
            R.id.Saved_Cocktails -> SAVED
            else -> null
        }
    }
}
