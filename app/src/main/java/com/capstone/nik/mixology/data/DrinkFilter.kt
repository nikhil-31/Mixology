package com.capstone.nik.mixology.data

import android.net.Uri
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

    val contentUri: Uri
        get() = when (this) {
            ALCOHOLIC -> DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC
            NON_ALCOHOLIC -> DrinkProvider.NonAlcoholic.CONTENT_URI_NON_ALCOHOLIC
            COCKTAIL -> DrinkProvider.Cocktail.CONTENT_URI_COCKTAIL
            ORDINARY_DRINK -> DrinkProvider.OrdinaryDrink.CONTENT_URI_ORDINARY_DRINK
            GIN -> DrinkProvider.Gin.CONTENT_URI_GIN
            VODKA -> DrinkProvider.Vodka.CONTENT_URI_VODKA
            COCKTAIL_GLASS -> DrinkProvider.CocktailGlass.CONTENT_URI_COCKTAIL_GLASS
            HIGHBALL_GLASS -> DrinkProvider.ChampagneFlute.CONTENT_URI_HIGHBALL_GLASS
            SAVED -> DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED
        }

    companion object {
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
