package com.capstone.nik.mixology.ui

import android.content.Intent
import com.capstone.nik.mixology.data.Drink

object DrinkIntents {
    const val ACTION_OPEN_DRINK = "com.capstone.nik.mixology.action.OPEN_DRINK"
    const val EXTRA_ID = "drink_id"
    const val EXTRA_NAME = "drink_name"
    const val EXTRA_THUMB = "drink_thumb"
}

fun Intent.drinkExtra(): Drink? {
    val id = getStringExtra(DrinkIntents.EXTRA_ID) ?: return null
    if (id.isBlank()) return null
    return Drink(
        id = id,
        name = getStringExtra(DrinkIntents.EXTRA_NAME).orEmpty(),
        thumb = getStringExtra(DrinkIntents.EXTRA_THUMB).orEmpty(),
    )
}

fun Intent.putDrinkExtra(drink: Drink): Intent {
    putExtra(DrinkIntents.EXTRA_ID, drink.id)
    putExtra(DrinkIntents.EXTRA_NAME, drink.name)
    putExtra(DrinkIntents.EXTRA_THUMB, drink.thumb)
    return this
}
