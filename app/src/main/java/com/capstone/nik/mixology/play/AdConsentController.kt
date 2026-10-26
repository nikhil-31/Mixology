package com.capstone.nik.mixology.play

import android.app.Activity
import android.content.Context
import com.capstone.nik.mixology.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdConsentController @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val canRequestAdsFlow = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = canRequestAdsFlow.asStateFlow()

    private val privacyOptionsRequiredFlow = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = privacyOptionsRequiredFlow.asStateFlow()

    private var started = false

    fun start(activity: Activity) {
        if (BuildConfig.DEBUG || started) return
        started = true
        val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    publish(consentInformation)
                    if (consentInformation.canRequestAds()) {
                        MobileAds.initialize(appContext)
                    }
                }
            },
            { publish(consentInformation) },
        )
    }

    fun presentPrivacyOptions(activity: Activity) {
        if (BuildConfig.DEBUG) return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            publish(UserMessagingPlatform.getConsentInformation(appContext))
        }
    }

    private fun publish(info: ConsentInformation) {
        canRequestAdsFlow.value = info.canRequestAds()
        privacyOptionsRequiredFlow.value = info.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }
}
