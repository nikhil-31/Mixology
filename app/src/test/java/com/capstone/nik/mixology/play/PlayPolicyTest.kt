package com.capstone.nik.mixology.play

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReviewPromptPolicyTest {

    @Test
    fun shouldPrompt_requiresSavesInstallAgeAndCooldown() {
        val now = TimeUnit.DAYS.toMillis(200)
        assertFalse(
            ReviewPromptPolicy.shouldPrompt(
                ReviewPromptState(saveCount = 1, firstOpenAt = 1L),
                now,
            ),
        )
        assertFalse(
            ReviewPromptPolicy.shouldPrompt(
                ReviewPromptState(
                    saveCount = 2,
                    firstOpenAt = now - TimeUnit.DAYS.toMillis(1),
                ),
                now,
            ),
        )
        assertTrue(
            ReviewPromptPolicy.shouldPrompt(
                ReviewPromptState(
                    saveCount = 2,
                    firstOpenAt = now - TimeUnit.DAYS.toMillis(3),
                ),
                now,
            ),
        )
        assertFalse(
            ReviewPromptPolicy.shouldPrompt(
                ReviewPromptState(
                    saveCount = 5,
                    firstOpenAt = 1L,
                    lastPromptAt = now - TimeUnit.DAYS.toMillis(30),
                ),
                now,
            ),
        )
        assertTrue(
            ReviewPromptPolicy.shouldPrompt(
                ReviewPromptState(
                    saveCount = 5,
                    firstOpenAt = 1L,
                    lastPromptAt = now - TimeUnit.DAYS.toMillis(90),
                ),
                now,
            ),
        )
    }
}

class PlayUpdatePolicyTest {

    @Test
    fun action_prefersCompleteThenImmediateThenFlexible() {
        assertEquals(
            PlayUpdateAction.COMPLETE,
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = true,
                    inProgress = false,
                    downloaded = true,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                ),
            ),
        )
        assertEquals(
            PlayUpdateAction.IMMEDIATE,
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = true,
                    inProgress = false,
                    downloaded = false,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                    priority = 4,
                ),
            ),
        )
        assertEquals(
            PlayUpdateAction.IMMEDIATE,
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = true,
                    inProgress = false,
                    downloaded = false,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                    stalenessDays = 14,
                ),
            ),
        )
        assertEquals(
            PlayUpdateAction.FLEXIBLE,
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = true,
                    inProgress = false,
                    downloaded = false,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                ),
            ),
        )
        assertEquals(
            PlayUpdateAction.IMMEDIATE,
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = false,
                    inProgress = true,
                    downloaded = false,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                ),
            ),
        )
        assertNull(
            PlayUpdatePolicy.action(
                PlayUpdateSnapshot(
                    available = false,
                    inProgress = false,
                    downloaded = false,
                    flexibleAllowed = true,
                    immediateAllowed = true,
                ),
            ),
        )
    }
}
