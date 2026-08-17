package com.capstone.nik.mixology.ui

import android.content.Intent
import com.capstone.nik.mixology.Model.Cocktail

object DrinkIntents {
    const val ACTION_OPEN_DRINK = "com.capstone.nik.mixology.action.OPEN_DRINK"
    const val EXTRA_ID = "drink_id"
    const val EXTRA_NAME = "drink_name"
    const val EXTRA_THUMB = "drink_thumb"
}

fun Intent.drinkExtra(): Cocktail? {
    val id = getStringExtra(DrinkIntents.EXTRA_ID) ?: return null
    if (id.isBlank()) return null
    return Cocktail(
        id,
        getStringExtra(DrinkIntents.EXTRA_NAME).orEmpty(),
        getStringExtra(DrinkIntents.EXTRA_THUMB).orEmpty(),
    )
}

fun Intent.putDrinkExtra(cocktail: Cocktail): Intent {
    putExtra(DrinkIntents.EXTRA_ID, cocktail.getmDrinkId())
    putExtra(DrinkIntents.EXTRA_NAME, cocktail.getmDrinkName())
    putExtra(DrinkIntents.EXTRA_THUMB, cocktail.getmDrinkThumb())
    return this
}
