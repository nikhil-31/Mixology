package com.capstone.nik.mixology.ui.tour

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Whether this device has been shown the welcome tour. Kept per device, not per account, so it
 * is not cleared on sign-out.
 */
object WelcomeTourPreferences {
    internal const val PREFS = "mixology"
    internal const val KEY_SEEN = "welcome_tour_seen"

    fun isSeen(context: Context): Boolean = prefs(context).getBoolean(KEY_SEEN, false)

    fun setSeen(context: Context, seen: Boolean) {
        prefs(context).edit().putBoolean(KEY_SEEN, seen).apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}

@Composable
fun rememberWelcomeTourSeen(): Boolean {
    val context = LocalContext.current.applicationContext
    var seen by remember { mutableStateOf(WelcomeTourPreferences.isSeen(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences(WelcomeTourPreferences.PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == WelcomeTourPreferences.KEY_SEEN) {
                seen = WelcomeTourPreferences.isSeen(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return seen
}
