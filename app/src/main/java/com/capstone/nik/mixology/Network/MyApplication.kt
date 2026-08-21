package com.capstone.nik.mixology.Network

import android.app.Application
import com.capstone.nik.mixology.BuildConfig
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject lateinit var networkMonitor: NetworkMonitor
    @Inject lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate() {
        super.onCreate()
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
        analyticsTracker.setCollectionEnabled(!BuildConfig.DEBUG)
        networkMonitor.start()
        MobileAds.initialize(this)
    }
}
