package com.capstone.nik.mixology.ui.mvi

import app.cash.turbine.test
import com.capstone.nik.mixology.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private class EchoViewModel : MviViewModel<String, Int, String>(0) {
    override fun onIntent(intent: String) {
        setState { this + 1 }
        sendEffect(intent)
    }
}

class MviViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun anEffectSentWhileNothingCollectsWaitsForTheScreen() = runTest {
        val viewModel = EchoViewModel()

        // As during a rotation: the old screen is gone and the new one isn't collecting yet.
        viewModel.onIntent("open")
        viewModel.onIntent("close")

        viewModel.effects.test {
            assertEquals("open", awaitItem())
            assertEquals("close", awaitItem())
            expectNoEvents()
        }
        assertEquals(2, viewModel.state.value)
    }

    @Test
    fun anEffectIsDeliveredOnce() = runTest {
        val viewModel = EchoViewModel()
        viewModel.onIntent("open")

        viewModel.effects.test { assertEquals("open", awaitItem()) }
        viewModel.effects.test { expectNoEvents() }
    }
}
