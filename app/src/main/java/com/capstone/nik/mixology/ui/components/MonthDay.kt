package com.capstone.nik.mixology.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** "Nov 1" in the user's locale. Credits reset at midnight UTC, so the day is read in UTC. */
@Composable
fun rememberMonthDay(date: Date): String {
    val locale = Locale.getDefault()
    return remember(date, locale) {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMd")
        SimpleDateFormat(pattern, locale).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(date)
    }
}
