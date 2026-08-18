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
    val extraId = getStringExtra(DrinkIntents.EXTRA_ID)?.takeIf { it.isNotBlank() }
    if (extraId != null) {
        return Drink(
            id = extraId,
            name = getStringExtra(DrinkIntents.EXTRA_NAME).orEmpty(),
            thumb = getStringExtra(DrinkIntents.EXTRA_THUMB).orEmpty(),
        )
    }
    val uri = data ?: return null
    if (uri.scheme != "mixology" || uri.host != "drink") return null
    val id = uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: return null
    return Drink(
        id = id,
        name = uri.getQueryParameter("name").orEmpty(),
        thumb = uri.getQueryParameter("thumb").orEmpty(),
    )
}

fun Intent.putDrinkExtra(drink: Drink): Intent {
    putExtra(DrinkIntents.EXTRA_ID, drink.id)
    putExtra(DrinkIntents.EXTRA_NAME, drink.name)
    putExtra(DrinkIntents.EXTRA_THUMB, drink.thumb)
    return this
}
