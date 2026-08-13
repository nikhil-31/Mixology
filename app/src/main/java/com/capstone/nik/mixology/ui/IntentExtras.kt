package com.capstone.nik.mixology.ui

import android.content.Intent
import android.os.Build
import com.capstone.nik.mixology.Model.Cocktail

fun Intent.getCocktailExtra(key: String): Cocktail? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, Cocktail::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
}
