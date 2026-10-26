package com.capstone.nik.mixology.di

import com.capstone.nik.mixology.Network.NetworkMonitor
import com.capstone.nik.mixology.analytics.AnalyticsTracker
import com.capstone.nik.mixology.play.AdConsentController
import com.capstone.nik.mixology.play.PlayReviewController
import com.capstone.nik.mixology.play.PlayUpdateController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun networkMonitor(): NetworkMonitor
    fun analyticsTracker(): AnalyticsTracker
    fun playUpdateController(): PlayUpdateController
    fun playReviewController(): PlayReviewController
    fun adConsentController(): AdConsentController
}
