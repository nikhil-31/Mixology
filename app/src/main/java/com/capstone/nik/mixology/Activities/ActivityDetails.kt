package com.capstone.nik.mixology.Activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.getCocktailExtra
import com.capstone.nik.mixology.ui.details.DrinkDetailsRoute
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivityDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cocktail = intent.getCocktailExtra(getString(R.string.intent_details_intent_cocktail))
        if (cocktail == null) {
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                DrinkDetailsRoute(
                    cocktail = cocktail,
                    showUpNavigation = true,
                    onBack = { finish() },
                )
            }
        }
    }
}
