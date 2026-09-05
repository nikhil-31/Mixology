package com.capstone.nik.mixology.play

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.ktx.requestCompleteUpdate
import com.capstone.nik.mixology.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class PlayUpdateEvent {
    Downloaded,
    UpToDate,
}

@Singleton
class PlayUpdateController @Inject constructor(
    private val appUpdateManager: AppUpdateManager,
) {
    private val _events = MutableSharedFlow<PlayUpdateEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<PlayUpdateEvent> = _events.asSharedFlow()

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            _events.tryEmit(PlayUpdateEvent.Downloaded)
        }
    }

    private var listening = false

    suspend fun start(activity: Activity, userRequested: Boolean = false) {
        if (BuildConfig.DEBUG) {
            if (userRequested) _events.tryEmit(PlayUpdateEvent.UpToDate)
            return
        }
        ensureListening()
        val info = runCatching { appUpdateManager.requestAppUpdateInfo() }.getOrElse {
            if (userRequested) _events.tryEmit(PlayUpdateEvent.UpToDate)
            return
        }
        val snapshot = PlayUpdateSnapshot(
            available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE,
            inProgress = info.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS,
            downloaded = info.installStatus() == InstallStatus.DOWNLOADED,
            flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
            immediateAllowed = info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE),
            priority = info.updatePriority(),
            stalenessDays = info.clientVersionStalenessDays(),
        )
        when (PlayUpdatePolicy.action(snapshot)) {
            PlayUpdateAction.COMPLETE -> _events.tryEmit(PlayUpdateEvent.Downloaded)
            PlayUpdateAction.FLEXIBLE -> startFlow(activity, info, AppUpdateType.FLEXIBLE)
            PlayUpdateAction.IMMEDIATE -> startFlow(activity, info, AppUpdateType.IMMEDIATE)
            null -> if (userRequested) _events.tryEmit(PlayUpdateEvent.UpToDate)
        }
    }

    suspend fun completeUpdate() {
        runCatching { appUpdateManager.requestCompleteUpdate() }
    }

    private fun ensureListening() {
        if (listening) return
        appUpdateManager.registerListener(listener)
        listening = true
    }

    private fun startFlow(
        activity: Activity,
        info: com.google.android.play.core.appupdate.AppUpdateInfo,
        type: Int,
    ) {
        runCatching {
            appUpdateManager.startUpdateFlow(
                info,
                activity,
                AppUpdateOptions.newBuilder(type).build(),
            )
        }
    }
}
