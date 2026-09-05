package com.capstone.nik.mixology.play

import android.app.Activity
import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PlayReviewControllerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onDrinkSaved_emitsPromptWhenPolicyPasses() = runTest {
        val now = TimeUnit.DAYS.toMillis(10)
        val store = MemoryReviewPromptStore(
            ReviewPromptState(saveCount = 1, firstOpenAt = 1L),
        )
        val client = RecordingReviewClient()
        val controller = PlayReviewController(
            store = store,
            client = client,
            enabled = true,
            now = { now },
        )
        val prompt = async { controller.prompts.first() }
        runCurrent()
        controller.onDrinkSaved()
        prompt.await()
        assertEquals(2, store.state().saveCount)
        assertFalse(client.launched)
    }

    @Test
    fun launch_marksPromptedAfterClientCall() = runTest {
        val now = TimeUnit.DAYS.toMillis(10)
        val store = MemoryReviewPromptStore(
            ReviewPromptState(saveCount = 2, firstOpenAt = 1L),
        )
        val client = RecordingReviewClient()
        val controller = PlayReviewController(
            store = store,
            client = client,
            enabled = true,
            now = { now },
        )
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        controller.launch(activity)
        assertTrue(client.launched)
        assertEquals(now, store.state().lastPromptAt)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun disabled_doesNotPrompt() = runTest {
        val controller = PlayReviewController.disabled()
        val collected = mutableListOf<Unit>()
        val job = launch { controller.prompts.collect { collected += it } }
        runCurrent()
        controller.onDrinkSaved()
        controller.onDrinkSaved()
        runCurrent()
        job.cancel()
        assertTrue(collected.isEmpty())
    }

    @Test
    fun onOpenedListing_marksPrompted() {
        val now = TimeUnit.DAYS.toMillis(10)
        val store = MemoryReviewPromptStore(
            ReviewPromptState(saveCount = 2, firstOpenAt = 1L),
        )
        val controller = PlayReviewController(
            store = store,
            client = RecordingReviewClient(),
            enabled = true,
            now = { now },
        )
        controller.onOpenedListing()
        assertEquals(now, store.state().lastPromptAt)
        assertFalse(ReviewPromptPolicy.shouldPrompt(store.state(), now))
    }

    private class RecordingReviewClient : PlayReviewClient {
        var launched = false
        override suspend fun launch(activity: Activity): Boolean {
            launched = true
            return true
        }
    }
}
