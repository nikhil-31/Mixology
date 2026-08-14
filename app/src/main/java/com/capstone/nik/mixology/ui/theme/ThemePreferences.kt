package com.capstone.nik.mixology.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.find { it.name == value } ?: SYSTEM
    }
}

object ThemePreferences {
    internal const val PREFS = "mixology"
    internal const val KEY_THEME_MODE = "theme_mode"

    fun get(context: Context): ThemeMode {
        val stored = prefs(context).getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.fromStorage(stored)
    }

    fun set(context: Context, mode: ThemeMode) {
        prefs(context).edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }
}

@Composable
fun rememberThemeMode(): ThemeMode {
    val context = LocalContext.current.applicationContext
    var mode by remember { mutableStateOf(ThemePreferences.get(context)) }
    DisposableEffect(context) {
        val prefs = context.getSharedPreferences(ThemePreferences.PREFS, Context.MODE_PRIVATE)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == ThemePreferences.KEY_THEME_MODE) {
                mode = ThemePreferences.get(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return mode
}
