package com.capstone.nik.mixology.ui.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstone.nik.mixology.billing.Access
import com.capstone.nik.mixology.billing.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Decides whether the paywall covers the app, and keeps access fresh. */
@HiltViewModel
class SubscriptionGateViewModel @Inject constructor(
    private val subscriptions: SubscriptionRepository,
) : ViewModel() {
    val showPaywall: StateFlow<Boolean> = subscriptions.showPaywall
    val access: StateFlow<Access> = subscriptions.access

    /** On sign-in and whenever the app comes back to the foreground. */
    fun refresh() {
        viewModelScope.launch { subscriptions.refreshOnLaunch() }
    }

    fun openPaywall() = subscriptions.openPaywall()
}
