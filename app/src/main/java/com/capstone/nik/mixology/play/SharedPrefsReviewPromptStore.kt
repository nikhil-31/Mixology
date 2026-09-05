package com.capstone.nik.mixology.play

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsReviewPromptStore(
    context: Context,
) : ReviewPromptStore {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun state(): ReviewPromptState = ReviewPromptState(
        saveCount = prefs.getInt(KEY_SAVE_COUNT, 0),
        firstOpenAt = prefs.getLong(KEY_FIRST_OPEN, 0L),
        lastPromptAt = prefs.getLong(KEY_LAST_PROMPT, 0L),
    )

    override fun recordOpen(now: Long) {
        if (prefs.getLong(KEY_FIRST_OPEN, 0L) == 0L) {
            prefs.edit().putLong(KEY_FIRST_OPEN, now).apply()
        }
    }

    override fun recordSave() {
        prefs.edit().putInt(KEY_SAVE_COUNT, prefs.getInt(KEY_SAVE_COUNT, 0) + 1).apply()
    }

    override fun markPrompted(now: Long) {
        prefs.edit().putLong(KEY_LAST_PROMPT, now).apply()
    }

    private companion object {
        const val PREFS = "mixology"
        const val KEY_SAVE_COUNT = "play_review_save_count"
        const val KEY_FIRST_OPEN = "play_review_first_open_at"
        const val KEY_LAST_PROMPT = "play_review_last_prompt_at"
    }
}
