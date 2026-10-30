package com.capstone.nik.mixology.ui.recipes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.components.rememberMonthDay
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.paywall.SubscriptionGateViewModel
import java.util.Date

@Composable
fun ImportRecipeRoute(
    onBack: () -> Unit,
    /** Opens the saved recipe [id] in the editor. */
    onImported: (id: String) -> Unit,
    viewModel: ImportRecipeViewModel = hiltViewModel(),
    subscriptionGate: SubscriptionGateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chatCredits by subscriptionGate.chatCredits.collectAsStateWithLifecycle()
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is ImportRecipeEffect.Imported -> {
                // A toast, so the note outlives this screen as the editor opens.
                if (effect.duplicate) {
                    Toast.makeText(context, R.string.recipe_import_duplicate, Toast.LENGTH_SHORT).show()
                }
                onImported(effect.id)
            }
        }
    }
    ImportRecipeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        creditsResetAt = chatCredits?.resetsAt,
    )
}

@Composable
fun ImportRecipeScreen(
    state: ImportRecipeUiState,
    onIntent: (ImportRecipeIntent) -> Unit,
    onBack: () -> Unit,
    creditsResetAt: Date? = null,
) {
    val clipboard = LocalClipboardManager.current
    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("recipe_import_back")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_desc_up_navigation),
                    )
                }
                Text(
                    text = stringResource(R.string.recipe_import_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.recipe_import_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = state.text,
                    onValueChange = { onIntent(ImportRecipeIntent.TextChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_import_text"),
                    label = { Text(stringResource(R.string.recipe_import_field)) },
                    minLines = 4,
                    maxLines = 10,
                    enabled = !state.importing,
                )
                OutlinedButton(
                    onClick = {
                        clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let {
                            onIntent(ImportRecipeIntent.TextChanged(it))
                        }
                    },
                    enabled = !state.importing,
                    modifier = Modifier.testTag("recipe_import_paste"),
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = null)
                    Text(
                        text = stringResource(R.string.recipe_import_paste),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                state.error?.let { error ->
                    Text(
                        text = error.message(creditsResetAt),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recipe_import_error"),
                    )
                }
                Button(
                    onClick = { onIntent(ImportRecipeIntent.Import) },
                    enabled = state.canImport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recipe_import_submit"),
                ) {
                    if (state.importing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.recipe_import_reading))
                    } else {
                        Text(stringResource(R.string.recipe_import_submit))
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportRecipeError.message(creditsResetAt: Date?): String = when (this) {
    is ImportRecipeError.Server -> detail
    ImportRecipeError.Unreachable -> stringResource(R.string.recipe_import_unreachable)
    ImportRecipeError.OutOfCredits -> creditsResetAt?.let {
        stringResource(R.string.ada_out_of_credits, rememberMonthDay(it))
    } ?: stringResource(R.string.ada_out_of_credits_no_date)
    ImportRecipeError.Generic -> stringResource(R.string.recipe_import_error_generic)
}
