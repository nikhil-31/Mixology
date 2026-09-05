package com.capstone.nik.mixology.play

import java.util.concurrent.TimeUnit

data class ReviewPromptState(
    val saveCount: Int = 0,
    val firstOpenAt: Long = 0L,
    val lastPromptAt: Long = 0L,
)

object ReviewPromptPolicy {
    const val MIN_SAVES = 2
    const val MIN_DAYS_SINCE_INSTALL = 3
    const val COOLDOWN_DAYS = 90

    fun shouldPrompt(state: ReviewPromptState, now: Long): Boolean {
        if (state.saveCount < MIN_SAVES) return false
        val firstOpen = state.firstOpenAt.takeIf { it > 0L } ?: now
        if (now - firstOpen < TimeUnit.DAYS.toMillis(MIN_DAYS_SINCE_INSTALL.toLong())) return false
        if (state.lastPromptAt > 0L &&
            now - state.lastPromptAt < TimeUnit.DAYS.toMillis(COOLDOWN_DAYS.toLong())
        ) {
            return false
        }
        return true
    }
}
