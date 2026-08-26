package com.capstone.nik.mixology.Network

import android.app.Application
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class NetworkMonitorTest {

    @Test
    fun forTests_startsWithConfiguredOnlineFlag() {
        assertTrue(NetworkMonitor.forTests(online = true).online.value)
        assertFalse(NetworkMonitor.forTests(online = false).online.value)
    }

    @Test
    fun retry_emitsRetrySignal() = runTest {
        val monitor = NetworkMonitor.forTests(online = true)
        monitor.retries.test {
            monitor.retry()
            awaitItem()
        }
    }
}
