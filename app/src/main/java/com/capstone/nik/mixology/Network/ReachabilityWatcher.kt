package com.capstone.nik.mixology.Network

import com.capstone.nik.mixology.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Asks the server whether it is answering. */
fun interface ServerProbe {
    /** True when the server sent any answer; false when it couldn't be reached. */
    suspend fun ping(): Boolean
}

/**
 * GETs `/health` through the app's client, so [ReachabilityInterceptor] records the result like
 * any other call. Any answer but 503 counts as reachable, the same rule the interceptor uses.
 */
class HealthProbe @Inject constructor(
    private val client: OkHttpClient,
) : ServerProbe {
    override suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(BuildConfig.MIXOLOGY_API_BASE_URL.trimEnd('/') + "/health")
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build()
        try {
            client.newCall(request).execute().use { it.code != SERVICE_UNAVAILABLE }
        } catch (e: IOException) {
            false
        }
    }

    private companion object {
        const val SERVICE_UNAVAILABLE = 503
    }
}

/**
 * Gets the app back on its feet after the server stops answering. While the device is online but
 * the server isn't, it checks again with backoff; Retry checks at once. When the server answers
 * again, or the device comes back online, it fires [NetworkMonitor.retry] so every screen reloads.
 * Without this the "can't reach" state cleared only if some screen happened to make a request.
 */
@Singleton
class ReachabilityWatcher internal constructor(
    private val monitor: NetworkMonitor,
    private val probe: ServerProbe,
    private val scope: CoroutineScope,
    private val firstDelay: Duration = 2.seconds,
    private val maxDelay: Duration = 30.seconds,
) {
    @Inject
    constructor(monitor: NetworkMonitor, probe: HealthProbe) : this(
        monitor,
        probe,
        CoroutineScope(SupervisorJob() + Dispatchers.Default),
    )

    private var started = false

    fun start() {
        if (started) return
        started = true

        // Online but no answer: keep checking, waiting longer each time.
        scope.launch {
            combine(monitor.online, monitor.serverReachable) { online, reachable -> online && !reachable }
                .distinctUntilChanged()
                .collectLatest { stuck ->
                    if (!stuck) return@collectLatest
                    var wait = firstDelay
                    while (true) {
                        delay(wait)
                        if (ping()) break
                        wait = (wait * 2).coerceAtMost(maxDelay)
                    }
                }
        }

        // Retry tapped while stuck: check now rather than at the next backoff step.
        scope.launch {
            monitor.retries.collect {
                if (monitor.online.value && !monitor.serverReachable.value) ping()
            }
        }

        // Back from no network or no server: tell every screen to load again.
        scope.launch {
            combine(monitor.online, monitor.serverReachable) { online, reachable -> online && reachable }
                .distinctUntilChanged()
                .drop(1)
                .filter { it }
                .collect { monitor.retry() }
        }
    }

    private suspend fun ping(): Boolean {
        val answered = try {
            probe.ping()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
        // The real probe goes through ReachabilityInterceptor; this keeps fakes and the state in step.
        if (answered) monitor.markReachable() else monitor.markUnreachable()
        return answered
    }
}
