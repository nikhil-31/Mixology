package com.capstone.nik.mixology.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.capstone.nik.mixology.Model.Cocktail
import com.capstone.nik.mixology.ui.drinkExtra
import com.capstone.nik.mixology.ui.main.MixologyApp
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivityMain : AppCompatActivity() {

    private var pendingDrink by mutableStateOf<Cocktail?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDrink = intent.drinkExtra()
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                MixologyApp(
                    windowSizeClass = calculateWindowSizeClass(this),
                    pendingDrink = pendingDrink,
                    onPendingDrinkConsumed = { pendingDrink = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDrink = intent.drinkExtra()
    }
}
