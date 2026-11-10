package com.capstone.nik.mixology.Network

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ReachabilityWatcherTest {

    private class FakeProbe(var answers: Boolean = false) : ServerProbe {
        var pings = 0
        override suspend fun ping(): Boolean {
            pings++
            return answers
        }
    }

    private fun TestScope.watch(monitor: NetworkMonitor, probe: FakeProbe): MutableList<Unit> {
        val retries = mutableListOf<Unit>()
        backgroundScope.launch { monitor.retries.collect { retries.add(it) } }
        ReachabilityWatcher(monitor, probe, backgroundScope, firstDelay = 2.seconds, maxDelay = 8.seconds).start()
        runCurrent()
        return retries
    }

    @Test
    fun reachableServer_isNotPinged() = runTest {
        val monitor = NetworkMonitor.forTests()
        val probe = FakeProbe()
        watch(monitor, probe)

        advanceTimeBy(60_000)
        assertEquals(0, probe.pings)
    }

    @Test
    fun unreachableServer_isPingedWithBackoff_andScreensReloadWhenItAnswers() = runTest {
        val monitor = NetworkMonitor.forTests()
        val probe = FakeProbe(answers = false)
        val retries = watch(monitor, probe)

        monitor.markUnreachable()
        runCurrent()
        advanceTimeBy(2_001) // 2s
        assertEquals(1, probe.pings)
        advanceTimeBy(4_000) // then 4s
        assertEquals(2, probe.pings)
        advanceTimeBy(8_000) // then 8s, the cap
        assertEquals(3, probe.pings)
        assertFalse(monitor.serverReachable.value)
        assertTrue(retries.isEmpty())

        probe.answers = true
        advanceTimeBy(8_000)
        runCurrent()
        assertEquals(4, probe.pings)
        assertTrue(monitor.serverReachable.value)
        assertEquals(1, retries.size)

        // Back to normal: no more pings.
        advanceTimeBy(60_000)
        assertEquals(4, probe.pings)
    }

    @Test
    fun retryTap_pingsAtOnce_insteadOfWaitingForTheNextStep() = runTest {
        val monitor = NetworkMonitor.forTests()
        val probe = FakeProbe(answers = true)
        val retries = watch(monitor, probe)

        monitor.markUnreachable()
        runCurrent()
        monitor.retry()
        runCurrent()

        assertEquals(1, probe.pings)
        assertTrue(monitor.serverReachable.value)
        // The tap itself, then the reload once the server answered.
        assertEquals(2, retries.size)
    }

    @Test
    fun failingProbe_keepsTheErrorShowing() = runTest {
        val monitor = NetworkMonitor.forTests()
        val throwing = object : ServerProbe {
            override suspend fun ping(): Boolean = error("boom")
        }
        ReachabilityWatcher(monitor, throwing, backgroundScope, firstDelay = 2.seconds).start()
        monitor.markUnreachable()
        runCurrent()
        advanceTimeBy(10_000)
        assertFalse(monitor.serverReachable.value)
    }
}
