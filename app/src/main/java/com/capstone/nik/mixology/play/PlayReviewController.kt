package com.capstone.nik.mixology.play

import android.app.Activity
import android.content.Context
import com.capstone.nik.mixology.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayReviewController(
    private val store: ReviewPromptStore,
    private val client: PlayReviewClient,
    private val enabled: Boolean,
    private val now: () -> Long,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        client: PlayReviewClient,
    ) : this(
        store = SharedPrefsReviewPromptStore(context),
        client = client,
        enabled = !BuildConfig.DEBUG,
        now = { System.currentTimeMillis() },
    )

    private val _prompts = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val prompts: SharedFlow<Unit> = _prompts.asSharedFlow()

    fun onAppOpen() {
        store.recordOpen(now())
    }

    fun onDrinkSaved() {
        store.recordSave()
        if (enabled && ReviewPromptPolicy.shouldPrompt(store.state(), now())) {
            _prompts.tryEmit(Unit)
        }
    }

    fun onOpenedListing() {
        store.markPrompted(now())
    }

    suspend fun launch(activity: Activity) {
        if (!enabled) return
        val snapshot = store.state()
        if (!ReviewPromptPolicy.shouldPrompt(snapshot, now())) return
        runCatching { client.launch(activity) }
        store.markPrompted(now())
    }

    companion object {
        fun disabled(): PlayReviewController = PlayReviewController(
            store = MemoryReviewPromptStore(),
            client = NoOpPlayReviewClient,
            enabled = false,
            now = { 0L },
        )
    }
}
