package com.capstone.nik.mixology.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.search.SearchRoute
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivitySearch : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val query = intent.getStringExtra(getString(R.string.intent_search_intent_query)).orEmpty()
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                SearchRoute(
                    initialQuery = query,
                    onBack = { finish() },
                    onDrinkClick = { cocktail ->
                        startActivity(
                            Intent(this, ActivityDetails::class.java)
                                .putExtra(getString(R.string.intent_details_intent_cocktail), cocktail),
                        )
                    },
                )
            }
        }
    }
}
