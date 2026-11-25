package com.capstone.nik.mixology.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.capstone.nik.mixology.ui.tour.WelcomeTourPreferences
import com.capstone.nik.mixology.ui.tour.WelcomeTourScreen
import com.capstone.nik.mixology.ui.tour.rememberWelcomeTourSeen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.billing.trialEndingDays
import com.capstone.nik.mixology.ui.auth.AuthRoute
import com.capstone.nik.mixology.ui.auth.SessionViewModel
import com.capstone.nik.mixology.ui.components.showsBannerAds
import com.capstone.nik.mixology.ui.drinkExtra
import com.capstone.nik.mixology.ui.sharedImportText
import com.capstone.nik.mixology.ui.main.MixologyApp
import com.capstone.nik.mixology.ui.paywall.PaywallRoute
import com.capstone.nik.mixology.ui.paywall.SubscriptionGateViewModel
import com.capstone.nik.mixology.ui.theme.MixologyTheme

@AndroidEntryPoint
class ActivityMain : AppCompatActivity() {

    private var pendingDrink by mutableStateOf<Drink?>(null)
    /** Text shared from another app, waiting to be imported as a recipe. */
    private var pendingImport by mutableStateOf<String?>(null)
    private val sessionViewModel: SessionViewModel by viewModels()
    private val subscriptionGate: SubscriptionGateViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Not again when the activity is recreated, such as on rotation.
        if (savedInstanceState == null) {
            pendingDrink = intent.drinkExtra()
            pendingImport = intent.sharedImportText()
        }
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                val session by sessionViewModel.session.collectAsStateWithLifecycle()
                val showPaywall by subscriptionGate.showPaywall.collectAsStateWithLifecycle()
                val access by subscriptionGate.access.collectAsStateWithLifecycle()
                val signedIn = session != null
                LaunchedEffect(signedIn) {
                    if (signedIn) subscriptionGate.refresh()
                }
                // Nothing in the app opens without an account; a pending drink waits for sign-in.
                // Once the trial is over, nothing opens without a subscription either.
                if (session == null) {
                    AuthRoute()
                } else if (showPaywall) {
                    PaywallRoute()
                } else {
                    MixologyApp(
                        windowSizeClass = calculateWindowSizeClass(this),
                        pendingDrink = pendingDrink,
                        onPendingDrinkConsumed = { pendingDrink = null },
                        pendingImport = pendingImport,
                        onPendingImportConsumed = { pendingImport = null },
                        adsEligible = showsBannerAds(access),
                        trialDaysLeft = trialEndingDays(access),
                        onSubscribe = subscriptionGate::openPaywall,
                    )
                    // Over the app rather than instead of it, so a replay from Settings keeps
                    // where you were, and a pending drink or import is there when it closes.
                    if (!rememberWelcomeTourSeen()) {
                        val context = LocalContext.current
                        val finishTour = { WelcomeTourPreferences.setSeen(context, true) }
                        Dialog(
                            onDismissRequest = finishTour,
                            properties = DialogProperties(
                                usePlatformDefaultWidth = false,
                                decorFitsSystemWindows = false,
                            ),
                        ) {
                            // The dialog has its own window, whose bar icons don't follow the
                            // activity's.
                            val view = LocalView.current
                            val lightBackground = MaterialTheme.colorScheme.background.luminance() > 0.5f
                            SideEffect {
                                (view.parent as? DialogWindowProvider)?.window?.let { window ->
                                    WindowCompat.getInsetsController(window, view).apply {
                                        isAppearanceLightStatusBars = lightBackground
                                        isAppearanceLightNavigationBars = lightBackground
                                    }
                                }
                            }
                            WelcomeTourScreen(onDone = finishTour)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Catches renewals, cancellations and purchases made outside the app.
        if (sessionViewModel.session.value != null) subscriptionGate.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDrink = intent.drinkExtra()
        intent.sharedImportText()?.let { pendingImport = it }
    }
}
