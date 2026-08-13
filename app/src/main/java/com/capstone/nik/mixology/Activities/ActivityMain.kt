package com.capstone.nik.mixology.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.main.MixologyApp
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivityMain : AppCompatActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                MixologyApp(
                    windowSizeClass = calculateWindowSizeClass(this),
                    onOpenSearch = { query ->
                        startActivity(
                            Intent(this, ActivitySearch::class.java)
                                .putExtra(getString(R.string.intent_search_intent_query), query),
                        )
                    },
                    onOpenDetails = { cocktail ->
                        startActivity(
                            Intent(this, ActivityDetails::class.java)
                                .putExtra(getString(R.string.intent_details_intent_cocktail), cocktail),
                        )
                    },
                    onSignOut = {
                        startActivity(Intent(this, ActivityLogin::class.java))
                    },
                )
            }
        }
    }
}
