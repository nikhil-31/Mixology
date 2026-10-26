package com.capstone.nik.mixology.ui.paywall

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.play.findActivity
import com.capstone.nik.mixology.ui.settings.PRIVACY_POLICY_URL

@Composable
fun PaywallRoute(viewModel: PaywallViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()
    PaywallScreen(
        state = state,
        onIntent = { intent -> viewModel.onIntent(intent) },
        onSubscribe = { activity?.let { viewModel.onIntent(PaywallIntent.Subscribe(it)) } },
    )
}

/** Full-screen paywall shown once the trial is over; nothing else in the app opens until then. */
@Composable
fun PaywallScreen(
    state: PaywallUiState,
    onIntent: (PaywallIntent) -> Unit,
    onSubscribe: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .testTag("paywall_screen"),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.canClose) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = { onIntent(PaywallIntent.Close) },
                        modifier = Modifier.testTag("paywall_close"),
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.paywall_close))
                    }
                }
            }
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = if (state.canClose) 0.dp else 24.dp)
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
            Text(
                text = stringResource(if (state.canClose) R.string.paywall_title_premium else R.string.paywall_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    R.string.paywall_benefit_ada,
                    R.string.paywall_benefit_bar,
                    R.string.paywall_benefit_saved,
                ).forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = stringResource(benefit),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }
            when {
                state.loadingPlans -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                state.plans.isEmpty() -> TextButton(
                    onClick = { onIntent(PaywallIntent.Load) },
                    modifier = Modifier.testTag("paywall_retry"),
                ) {
                    Text(stringResource(R.string.paywall_retry))
                }
                else -> state.plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        selected = plan.productId == state.selected,
                        enabled = !state.busy,
                        onSelect = { onIntent(PaywallIntent.Select(plan.productId)) },
                    )
                }
            }
            state.notice?.let { notice ->
                Text(
                    text = stringResource(notice.message()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notice == PaywallNotice.PENDING) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("paywall_notice"),
                )
            }
            Button(
                onClick = onSubscribe,
                enabled = state.canSubscribe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("paywall_subscribe"),
            ) {
                if (state.busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(R.string.paywall_subscribe))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onIntent(PaywallIntent.Restore) },
                    enabled = !state.busy,
                    modifier = Modifier.testTag("paywall_restore"),
                ) {
                    Text(stringResource(R.string.paywall_restore))
                }
                if (!state.canClose) {
                    TextButton(
                        onClick = { onIntent(PaywallIntent.SignOut) },
                        enabled = !state.busy,
                        modifier = Modifier.testTag("paywall_sign_out"),
                    ) {
                        Text(stringResource(R.string.paywall_sign_out))
                    }
                }
            }
            Text(
                text = stringResource(R.string.paywall_disclosure),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
                Text(stringResource(R.string.paywall_privacy), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: PaywallPlan,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        color = if (selected) colors.primaryContainer.copy(alpha = 0.35f) else colors.surface,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .testTag("paywall_plan_${plan.productId}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = null, enabled = enabled)
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) {
                Text(
                    text = stringResource(if (plan.yearly) R.string.paywall_yearly else R.string.paywall_monthly),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                )
                Text(
                    text = stringResource(
                        if (plan.yearly) R.string.paywall_price_per_year else R.string.paywall_price_per_month,
                        plan.price,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            if (plan.yearly) {
                Surface(shape = RoundedCornerShape(8.dp), color = colors.primary) {
                    Text(
                        text = stringResource(R.string.paywall_best_value),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

private fun PaywallNotice.message(): Int = when (this) {
    PaywallNotice.PENDING -> R.string.paywall_pending
    PaywallNotice.NOTHING_TO_RESTORE -> R.string.paywall_nothing_to_restore
    PaywallNotice.STORE_UNAVAILABLE -> R.string.paywall_store_unavailable
    PaywallNotice.PURCHASE_FAILED -> R.string.paywall_purchase_failed
}
