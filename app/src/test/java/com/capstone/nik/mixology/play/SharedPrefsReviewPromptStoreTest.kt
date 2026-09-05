package com.capstone.nik.mixology.play

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class SharedPrefsReviewPromptStoreTest {

    private lateinit var store: SharedPrefsReviewPromptStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.getSharedPreferences("mixology", 0).edit().clear().commit()
        store = SharedPrefsReviewPromptStore(context)
    }

    @Test
    fun recordOpenSaveAndPrompt_persistAcrossInstances() {
        store.recordOpen(10L)
        store.recordOpen(99L)
        store.recordSave()
        store.recordSave()
        store.markPrompted(20L)
        assertEquals(ReviewPromptState(saveCount = 2, firstOpenAt = 10L, lastPromptAt = 20L), store.state())

        val again = SharedPrefsReviewPromptStore(
            ApplicationProvider.getApplicationContext(),
        )
        assertEquals(ReviewPromptState(saveCount = 2, firstOpenAt = 10L, lastPromptAt = 20L), again.state())
    }
}
