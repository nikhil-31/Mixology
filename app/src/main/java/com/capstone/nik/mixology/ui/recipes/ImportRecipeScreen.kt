package com.capstone.nik.mixology.ui.recipes

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.billing.ChatCredits
import com.capstone.nik.mixology.ui.components.rememberMonthDay
import com.capstone.nik.mixology.ui.components.sourceLabel
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.paywall.SubscriptionGateViewModel
import kotlinx.coroutines.delay
import java.util.Date

/** The sites the server reads posts from, shown as chips. */
private val SUPPORTED_SITES = listOf(
    "Instagram", "TikTok", "YouTube", "Pinterest", "X", "Reddit", "Threads", "Facebook",
)

/** When each waiting step starts, in seconds; the server doesn't report progress. */
private const val READING_AFTER_SECONDS = 3
private const val SAVING_AFTER_SECONDS = 12

/** The first web link in [text], if any. */
internal fun firstLink(text: String): String? {
    val matcher = Patterns.WEB_URL.matcher(text)
    while (matcher.find()) {
        val link = matcher.group()
        // WEB_URL matches bare words like "1.5oz"; a post link has a scheme or a www.
        if (link.startsWith("http", ignoreCase = true) || link.startsWith("www.", ignoreCase = true)) {
            return link
        }
    }
    return null
}

/** The site of the first link in [text], such as "Instagram" or "liquor.com". */
internal fun linkSite(text: String): String? = firstLink(text)?.let { link ->
    val url = if (link.startsWith("http", ignoreCase = true)) link else "https://$link"
    sourceLabel(siteKey(url), url)
}

/** The server's name for a social link's site, so [sourceLabel] can spell it. */
private fun siteKey(url: String): String? {
    val host = runCatching { android.net.Uri.parse(url).host }.getOrNull()?.lowercase() ?: return null
    return listOf(
        "instagram" to listOf("instagram.com"),
        "tiktok" to listOf("tiktok.com"),
        "youtube" to listOf("youtube.com", "youtu.be"),
        "pinterest" to listOf("pinterest.", "pin.it"),
        "x" to listOf("x.com", "twitter.com"),
        "reddit" to listOf("reddit.com", "redd.it"),
        "threads" to listOf("threads.net", "threads.com"),
        "facebook" to listOf("facebook.com", "fb.watch"),
    ).firstOrNull { (_, hosts) -> hosts.any { host == it || host.endsWith(".$it") || host.startsWith(it) } }
        ?.first
}

@Composable
fun ImportRecipeRoute(
    onBack: () -> Unit,
    /** Opens the saved recipe [id] in the editor. */
    onImported: (id: String) -> Unit,
    /** Text shared from another app, imported as soon as the screen opens. */
    sharedText: String? = null,
    /** Ada's tab, where a link Import couldn't read waits for her. */
    onAskAda: () -> Unit = {},
    viewModel: ImportRecipeViewModel = hiltViewModel(),
    subscriptionGate: SubscriptionGateViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chatCredits by subscriptionGate.chatCredits.collectAsStateWithLifecycle()
    // Once per share, not again on rotation or on coming back to this screen.
    var sharedHandled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(sharedText) {
        if (sharedText != null && !sharedHandled) {
            sharedHandled = true
            viewModel.onIntent(ImportRecipeIntent.Shared(sharedText))
        }
    }

    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is ImportRecipeEffect.Imported -> {
                // A toast, so the note outlives this screen as the editor opens.
                if (effect.duplicate) {
                    Toast.makeText(context, R.string.recipe_import_duplicate, Toast.LENGTH_SHORT).show()
                }
                onImported(effect.id)
            }
            ImportRecipeEffect.OpenAda -> onAskAda()
        }
    }

    ImportRecipeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        credits = chatCredits,
    )
}

@Composable
fun ImportRecipeScreen(
    state: ImportRecipeUiState,
    onIntent: (ImportRecipeIntent) -> Unit,
    onBack: () -> Unit,
    credits: ChatCredits? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    // The field keeps its own cursor; the view model only holds the text.
    var field by remember { mutableStateOf(TextFieldValue(state.text)) }
    if (field.text != state.text) field = field.copy(text = state.text, selection = TextRange(state.text.length))
    val focusField: () -> Unit = {
        field = field.copy(selection = TextRange(field.text.length))
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
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
        },
        bottomBar = {
            if (!state.importing) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    credits?.let {
                        Text(
                            text = stringResource(R.string.recipe_import_credits_note, it.remaining),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Button(
                        onClick = { onIntent(ImportRecipeIntent.Import) },
                        enabled = state.canImport,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("recipe_import_submit"),
                    ) {
                        Text(stringResource(R.string.recipe_import_submit))
                    }
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = state.importing,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "import",
        ) { importing ->
            if (importing) {
                ImportProgress(
                    site = linkSite(state.text),
                    onCancel = { onIntent(ImportRecipeIntent.Cancel) },
                )
            } else {
                ImportForm(
                    field = field,
                    onFieldChange = {
                        field = it
                        if (it.text != state.text) onIntent(ImportRecipeIntent.TextChanged(it.text))
                    },
                    focusRequester = focusRequester,
                    error = state.error,
                    credits = credits,
                    onIntent = onIntent,
                    onFocusField = focusField,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ImportForm(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    error: ImportRecipeError?,
    credits: ChatCredits?,
    onIntent: (ImportRecipeIntent) -> Unit,
    onFocusField: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    val text = field.text
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.recipe_import_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag("recipe_import_sites"),
        ) {
            SUPPORTED_SITES.forEach { SiteChip(it) }
            SiteChip(stringResource(R.string.recipe_import_any_site))
        }
        OutlinedTextField(
            value = field,
            onValueChange = onFieldChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("recipe_import_text"),
            label = { Text(stringResource(R.string.recipe_import_field)) },
            minLines = 4,
            maxLines = 10,
            trailingIcon = {
                if (text.isEmpty()) {
                    IconButton(
                        onClick = {
                            clipboard.getText()?.text?.takeIf { it.isNotBlank() }?.let {
                                onIntent(ImportRecipeIntent.TextChanged(it))
                            }
                        },
                        modifier = Modifier.testTag("recipe_import_paste"),
                    ) {
                        Icon(Icons.Outlined.ContentPaste, contentDescription = stringResource(R.string.recipe_import_paste))
                    }
                } else {
                    IconButton(
                        onClick = { onIntent(ImportRecipeIntent.TextChanged("")) },
                        modifier = Modifier.testTag("recipe_import_clear"),
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.recipe_import_clear))
                    }
                }
            },
            supportingText = if (text.isBlank()) {
                null
            } else {
                {
                    val site = linkSite(text)
                    Text(
                        text = site?.let { stringResource(R.string.recipe_import_link_from, it) }
                            ?: stringResource(R.string.recipe_import_recipe_text),
                        modifier = Modifier.testTag("recipe_import_detected"),
                    )
                }
            },
        )
        error?.let {
            ImportErrorCard(
                error = it,
                credits = credits,
                onRetry = { onIntent(ImportRecipeIntent.Import) },
                onFocusField = {
                    onIntent(ImportRecipeIntent.DismissError)
                    onFocusField()
                },
                onDismiss = { onIntent(ImportRecipeIntent.DismissError) },
                onAskAda = { onIntent(ImportRecipeIntent.AskAda) },
            )
        }
        ShareTip()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SiteChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ShareTip() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth().testTag("recipe_import_tip"),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Outlined.IosShare,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.recipe_import_share_tip),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun ImportProgress(
    site: String?,
    onCancel: () -> Unit,
) {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            seconds++
        }
    }
    val step = when {
        seconds >= SAVING_AFTER_SECONDS -> R.string.recipe_import_step_saving
        seconds >= READING_AFTER_SECONDS -> R.string.recipe_import_step_reading
        else -> R.string.recipe_import_step_opening
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .testTag("recipe_import_progress"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        AnimatedContent(targetState = step, label = "step") { res ->
            Text(
                text = stringResource(res),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
        site?.let {
            Text(
                text = stringResource(R.string.recipe_import_from_site, it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = stringResource(R.string.recipe_import_wait_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .padding(top = 24.dp)
                .testTag("recipe_import_cancel"),
        ) {
            Text(stringResource(R.string.recipe_import_cancel))
        }
    }
}

@Composable
private fun ImportErrorCard(
    error: ImportRecipeError,
    credits: ChatCredits?,
    onRetry: () -> Unit,
    onFocusField: () -> Unit,
    onDismiss: () -> Unit,
    onAskAda: () -> Unit,
) {
    val content = error.content(credits?.resetsAt)
    // Ada can read a caption, or talk through a post the import couldn't.
    val offerAda = error is ImportRecipeError.Unreachable || error is ImportRecipeError.Server
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth().testTag("recipe_import_error"),
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
            Icon(
                imageVector = content.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                content.body?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (offerAda) {
                TextButton(onClick = onAskAda, modifier = Modifier.testTag("recipe_import_ask_ada")) {
                    Text(stringResource(R.string.recipe_import_ask_ada), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            val (label, action) = when (content.action) {
                ErrorAction.FocusCaption -> R.string.recipe_import_paste_caption to onFocusField
                ErrorAction.FocusText -> R.string.recipe_import_edit_text to onFocusField
                ErrorAction.Retry -> R.string.action_retry to onRetry
                ErrorAction.Dismiss -> android.R.string.ok to onDismiss
            }
            TextButton(onClick = action, modifier = Modifier.testTag("recipe_import_error_action")) {
                Text(stringResource(label), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

private enum class ErrorAction { FocusCaption, FocusText, Retry, Dismiss }

private class ErrorContent(
    val icon: ImageVector,
    val title: String,
    val body: String?,
    val action: ErrorAction,
)

@Composable
private fun ImportRecipeError.content(creditsResetAt: Date?): ErrorContent = when (this) {
    ImportRecipeError.Unreachable -> ErrorContent(
        Icons.Outlined.LinkOff,
        stringResource(R.string.recipe_import_unreachable_title),
        stringResource(R.string.recipe_import_unreachable),
        ErrorAction.FocusCaption,
    )
    is ImportRecipeError.NoConnection -> ErrorContent(
        Icons.Outlined.CloudOff,
        stringResource(error.messageRes),
        null,
        ErrorAction.Retry,
    )
    ImportRecipeError.OutOfCredits -> ErrorContent(
        Icons.Outlined.HourglassEmpty,
        stringResource(R.string.recipe_import_out_of_credits_title),
        creditsResetAt?.let { stringResource(R.string.ada_out_of_credits, rememberMonthDay(it)) }
            ?: stringResource(R.string.ada_out_of_credits_no_date),
        ErrorAction.Dismiss,
    )
    is ImportRecipeError.Server -> ErrorContent(
        Icons.Outlined.SearchOff,
        stringResource(R.string.recipe_import_not_found_title),
        detail,
        ErrorAction.FocusText,
    )
    ImportRecipeError.Generic -> ErrorContent(
        Icons.Outlined.ErrorOutline,
        stringResource(R.string.recipe_import_error_generic),
        null,
        ErrorAction.Retry,
    )
}
