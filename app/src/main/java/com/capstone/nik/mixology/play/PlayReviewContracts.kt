package com.capstone.nik.mixology.play

import android.app.Activity

fun interface PlayReviewClient {
    suspend fun launch(activity: Activity): Boolean
}

object NoOpPlayReviewClient : PlayReviewClient {
    override suspend fun launch(activity: Activity): Boolean = false
}

interface ReviewPromptStore {
    fun state(): ReviewPromptState
    fun recordOpen(now: Long)
    fun recordSave()
    fun markPrompted(now: Long)
}

class MemoryReviewPromptStore(
    initial: ReviewPromptState = ReviewPromptState(),
) : ReviewPromptStore {
    private var current = initial

    override fun state(): ReviewPromptState = current

    override fun recordOpen(now: Long) {
        if (current.firstOpenAt == 0L) {
            current = current.copy(firstOpenAt = now)
        }
    }

    override fun recordSave() {
        current = current.copy(saveCount = current.saveCount + 1)
    }

    override fun markPrompted(now: Long) {
        current = current.copy(lastPromptAt = now)
    }
}
