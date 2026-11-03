package com.capstone.nik.mixology.ui.ada

import android.content.Context
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.capstone.nik.mixology.ui.recipes.firstLink
import com.capstone.nik.mixology.ui.recipes.linkSite
import java.io.File
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkCardRailHeight
import com.capstone.nik.mixology.ui.components.DrinkCardRailWidth
import com.capstone.nik.mixology.ui.components.rememberMonthDay
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects
import com.capstone.nik.mixology.ui.paywall.SubscriptionGateViewModel
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun AdaRoute(
    onDrinkClick: (Drink) -> Unit,
    onOpenMore: (query: String, exclude: List<String>) -> Unit = { _, _ -> },
    onEditDraft: () -> Unit = {},
    viewModel: AdaViewModel = hiltViewModel(),
    subscriptionGate: SubscriptionGateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val chatCredits by subscriptionGate.chatCredits.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val speech = remember { AndroidAdaSpeechInput(context.applicationContext) }
    val voiceAvailable = remember { speech.available }
    val askMicrophone = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onIntent(AdaIntent.VoicePermission(granted))
    }
    val composerFocus = remember { FocusRequester() }
    val choosePhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onIntent(AdaIntent.SendPhoto(it)) }
    }
    // The camera writes into a cache file this app shares with it.
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { taken ->
        val uri = cameraUri
        if (taken && uri != null) viewModel.onIntent(AdaIntent.SendPhoto(uri))
    }
    val clipboard = LocalClipboardManager.current
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is AdaEffect.OpenDrink -> onDrinkClick(effect.drink)
            is AdaEffect.OpenMore -> onOpenMore(effect.query, effect.exclude)
            AdaEffect.FocusComposer -> runCatching { composerFocus.requestFocus() }
            AdaEffect.OpenDraftEditor -> onEditDraft()
            AdaEffect.RequestMicrophone -> {
                val permission = Manifest.permission.RECORD_AUDIO
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    viewModel.onIntent(AdaIntent.VoicePermission(granted = true))
                } else {
                    askMicrophone.launch(permission)
                }
            }
        }
    }
    LaunchedEffect(state.listening) {
        if (state.listening) {
            speech.start { event ->
                viewModel.onIntent(
                    when (event) {
                        is AdaSpeechEvent.Transcript -> AdaIntent.VoiceTranscript(event.text, event.final)
                        is AdaSpeechEvent.Ended -> AdaIntent.VoiceEnded(event.failed)
                    },
                )
            }
        } else {
            speech.stop()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.onIntent(AdaIntent.StopVoice)
    }
    DisposableEffect(speech) {
        onDispose {
            speech.release()
            viewModel.onIntent(AdaIntent.StopVoice)
        }
    }
    AdaScreen(
        state = state,
        onDraftChanged = { viewModel.onIntent(AdaIntent.DraftChanged(it)) },
        onSend = { viewModel.onIntent(AdaIntent.Send) },
        onRetry = { viewModel.onIntent(AdaIntent.Retry) },
        onSuggestion = { viewModel.onIntent(AdaIntent.UseSuggestion(it)) },
        onDrinkClick = { viewModel.onIntent(AdaIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(AdaIntent.ToggleSaved(it)) },
        onLoadOlder = { viewModel.onIntent(AdaIntent.LoadOlder) },
        onShowMore = { viewModel.onIntent(AdaIntent.OpenMore(it)) },
        voiceAvailable = voiceAvailable,
        creditsResetAt = chatCredits?.resetsAt,
        onToggleVoice = { viewModel.onIntent(AdaIntent.ToggleVoice) },
        onSaveRecipe = { viewModel.onIntent(AdaIntent.SaveRecipe(it)) },
        onDropRecipe = { viewModel.onIntent(AdaIntent.DropRecipe(it)) },
        onOpenRecipe = { viewModel.onIntent(AdaIntent.OpenRecipe(it)) },
        onEditRecipe = { viewModel.onIntent(AdaIntent.EditRecipe(it)) },
        onChoosePhoto = {
            choosePhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onTakePhoto = {
            val uri = adaCameraUri(context)
            cameraUri = uri
            takePhoto.launch(uri)
        },
        onImportSuggestion = {
            val text = clipboard.getText()?.text
            viewModel.onIntent(AdaIntent.UseImportSuggestion(text?.takeIf { firstLink(it) != null }))
        },
        composerFocus = composerFocus,
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        },
    )
}

@Composable
fun AdaScreen(
    state: AdaUiState,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onRetry: () -> Unit = {},
    onLoadOlder: () -> Unit = {},
    onShowMore: (messageId: Long) -> Unit = {},
    voiceAvailable: Boolean = false,
    /** When this month's credits come back, for the out-of-credits notice. */
    creditsResetAt: Date? = null,
    onToggleVoice: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSaveRecipe: (messageId: Long) -> Unit = {},
    onDropRecipe: (messageId: Long) -> Unit = {},
    onOpenRecipe: (messageId: Long) -> Unit = {},
    onEditRecipe: (messageId: Long) -> Unit = {},
    onChoosePhoto: () -> Unit = {},
    onTakePhoto: () -> Unit = {},
    onImportSuggestion: () -> Unit = {},
    composerFocus: FocusRequester = remember { FocusRequester() },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("ada_screen"),
    ) {
        // Signed-out users never get here: the app shows the login screen instead.
        if (state.signedIn) {
            AdaTranscript(
                state = state,
                onDraftChanged = onDraftChanged,
                onSend = onSend,
                onRetry = onRetry,
                onSuggestion = onSuggestion,
                onDrinkClick = onDrinkClick,
                onToggleSaved = onToggleSaved,
                onLoadOlder = onLoadOlder,
                onShowMore = onShowMore,
                voiceAvailable = voiceAvailable,
                creditsResetAt = creditsResetAt,
                onToggleVoice = onToggleVoice,
                onOpenSettings = onOpenSettings,
                onSaveRecipe = onSaveRecipe,
                onDropRecipe = onDropRecipe,
                onOpenRecipe = onOpenRecipe,
                onEditRecipe = onEditRecipe,
                onChoosePhoto = onChoosePhoto,
                onTakePhoto = onTakePhoto,
                onImportSuggestion = onImportSuggestion,
                composerFocus = composerFocus,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AdaTranscript(
    state: AdaUiState,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onSuggestion: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onLoadOlder: () -> Unit,
    onShowMore: (messageId: Long) -> Unit,
    voiceAvailable: Boolean,
    creditsResetAt: Date?,
    onToggleVoice: () -> Unit,
    onOpenSettings: () -> Unit,
    onSaveRecipe: (messageId: Long) -> Unit,
    onDropRecipe: (messageId: Long) -> Unit,
    onOpenRecipe: (messageId: Long) -> Unit,
    onEditRecipe: (messageId: Long) -> Unit,
    onChoosePhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onImportSuggestion: () -> Unit,
    composerFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val suggestions = listOf(
        stringResource(R.string.ada_suggestion_gin) to Icons.Outlined.LocalFlorist,
        stringResource(R.string.ada_suggestion_bitter) to Icons.Outlined.Nightlight,
        stringResource(R.string.ada_suggestion_easy) to Icons.Outlined.AutoAwesome,
    )
    // The list is laid out bottom-up (reverseLayout), so item 0 is the newest
    // message and the chat opens there without scrolling through history.
    val listState = rememberLazyListState()
    val newest = state.messages.lastOrNull()
    LaunchedEffect(newest?.id) {
        // A new message lands below the visible ones; bring it into view.
        if (newest != null) listState.animateScrollToItem(0)
    }
    LaunchedEffect(newest?.content, newest?.drinks?.size) {
        // Streaming text and drink cards grow the newest message; stay pinned
        // to it unless the user has scrolled up to read older messages.
        if (newest != null && listState.firstVisibleItemIndex == 0) listState.scrollToItem(0)
    }
    val nearOldest by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && last >= info.totalItemsCount - OLDER_PREFETCH
        }
    }
    LaunchedEffect(nearOldest, state.hasOlder, state.loadingOlder) {
        if (nearOldest && state.hasOlder && !state.loadingOlder) onLoadOlder()
    }
    Column(modifier = modifier.fillMaxWidth()) {
        when {
            state.loadingHistory && state.messages.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            state.messages.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        // Sits a little above centre so it doesn't crowd the composer.
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 56.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AdaAvatar(size = 88.dp, modifier = Modifier.padding(bottom = 24.dp))
                    Text(
                        text = stringResource(R.string.ada_greeting),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.ada_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Column(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .widthIn(max = 360.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        suggestions.forEachIndexed { index, (label, icon) ->
                            AdaSuggestion(
                                label = label,
                                icon = icon,
                                onClick = { onSuggestion(label) },
                                modifier = Modifier.testTag("ada_suggestion_$index"),
                            )
                        }
                        AdaSuggestion(
                            label = stringResource(R.string.ada_suggestion_import),
                            icon = Icons.Outlined.Link,
                            onClick = onImportSuggestion,
                            modifier = Modifier.testTag("ada_suggestion_import"),
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    reverseLayout = true,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("ada_messages"),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    val newestId = state.messages.lastOrNull()?.id
                    val newestFirst = state.messages.asReversed()
                    itemsIndexed(newestFirst, key = { _, message -> message.id }) { index, message ->
                        // Newest first: index - 1 is the message after this one, index + 1 the one before.
                        AdaMessageRow(
                            message = message,
                            continuesRun = newestFirst.getOrNull(index + 1)?.role == message.role,
                            endsRun = newestFirst.getOrNull(index - 1)?.role != message.role,
                            onDrinkClick = onDrinkClick,
                            onToggleSaved = onToggleSaved,
                            // Only the newest reply pages; older ones would page the wrong search.
                            onShowMore = { onShowMore(message.id) }.takeIf { message.id == newestId && !state.sending },
                            canAnswerRecipe = !state.sending,
                            onSaveRecipe = { onSaveRecipe(message.id) },
                            onDropRecipe = { onDropRecipe(message.id) },
                            onOpenRecipe = { onOpenRecipe(message.id) },
                            onEditRecipe = { onEditRecipe(message.id) },
                        )
                    }
                    if (state.loadingOlder) {
                        item(key = "ada_loading_older") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag("ada_loading_older"),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
        if (state.outOfCredits) {
            val resetsOn = creditsResetAt?.let { rememberMonthDay(it) }
            AdaNotice(
                text = if (resetsOn != null) {
                    stringResource(R.string.ada_out_of_credits, resetsOn)
                } else {
                    stringResource(R.string.ada_out_of_credits_no_date)
                },
                modifier = Modifier.testTag("ada_out_of_credits"),
                // Spent credits aren't a failure, so this one gets a calmer icon.
                icon = Icons.Outlined.Info,
            )
        }
        state.error?.let { detail ->
            AdaNotice(
                text = detail.ifBlank { stringResource(R.string.ada_error_generic) },
                modifier = Modifier.testTag("ada_error"),
                action = if (state.retryAvailable) {
                    {
                        TextButton(onClick = onRetry, modifier = Modifier.testTag("ada_retry")) {
                            Text(stringResource(R.string.action_retry))
                        }
                    }
                } else {
                    null
                },
            )
        }
        when (state.voiceProblem) {
            AdaVoiceProblem.Denied -> AdaNotice(
                text = stringResource(R.string.ada_mic_denied),
                modifier = Modifier.testTag("ada_mic_denied"),
                action = {
                    TextButton(onClick = onOpenSettings) {
                        Text(stringResource(R.string.ada_mic_open_settings))
                    }
                },
            )
            AdaVoiceProblem.Failed -> AdaNotice(
                text = stringResource(R.string.ada_mic_error),
                modifier = Modifier.testTag("ada_mic_error"),
            )
            null -> Unit
        }
        val linkSite = remember(state.draft) { linkSite(state.draft) }
        if (linkSite != null && !state.sending) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, end = 20.dp, top = 4.dp)
                    .testTag("ada_link_hint"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = stringResource(R.string.ada_link_hint, linkSite),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        AdaComposer(
            focusRequester = composerFocus,
            canAttach = !state.sending && !state.loadingHistory && !state.listening,
            onChoosePhoto = onChoosePhoto,
            onTakePhoto = onTakePhoto,
            draft = state.draft,
            canSend = state.draft.isNotBlank() && !state.sending && !state.loadingHistory,
            listening = state.listening,
            showMic = voiceAvailable && !state.sending,
            onDraftChanged = onDraftChanged,
            onSend = onSend,
            onToggleVoice = onToggleVoice,
        )
    }
}

/** A starter request, in the composer's bubble style. */
@Composable
private fun AdaSuggestion(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ComposerRadius),
        color = adaBubbleFill(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

@Composable
private fun AdaNotice(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.ErrorOutline,
    action: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
            action?.invoke()
        }
    }
}

@Composable
private fun AdaComposer(
    focusRequester: FocusRequester,
    canAttach: Boolean,
    onChoosePhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    draft: String,
    canSend: Boolean,
    listening: Boolean,
    showMic: Boolean,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onToggleVoice: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val colors = MaterialTheme.colorScheme
    // One bubble holds the field and its buttons; the outline shows focus and dictation.
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp),
        shape = RoundedCornerShape(ComposerRadius),
        color = adaBubbleFill(),
        border = BorderStroke(
            width = if (focused || listening) 1.5.dp else 1.dp,
            color = if (focused || listening) colors.primary else colors.outline,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            AttachPhotoButton(
                enabled = canAttach,
                onChoosePhoto = onChoosePhoto,
                onTakePhoto = onTakePhoto,
            )
            BasicTextField(
                value = draft,
                onValueChange = onDraftChanged,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .heightIn(min = ComposerButtonSize)
                    .testTag("ada_draft"),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
                cursorBrush = SolidColor(colors.primary),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
                interactionSource = interactionSource,
                decorationBox = { field ->
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (draft.isEmpty()) {
                            Text(
                                text = stringResource(if (listening) R.string.ada_listening else R.string.ada_prompt),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (listening) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                        field()
                    }
                },
            )
            if (showMic) {
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(ComposerButtonSize)
                        .testTag("ada_mic"),
                    colors = if (listening) {
                        IconButtonDefaults.iconButtonColors(
                            containerColor = colors.primary.copy(alpha = 0.14f),
                            contentColor = colors.primary,
                        )
                    } else {
                        IconButtonDefaults.iconButtonColors(contentColor = colors.onSurfaceVariant)
                    },
                ) {
                    Icon(
                        imageVector = if (listening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = stringResource(if (listening) R.string.ada_mic_stop else R.string.ada_mic_start),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(ComposerButtonSize)
                    .testTag("ada_send"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    disabledContainerColor = colors.onSurface.copy(alpha = 0.08f),
                    disabledContentColor = colors.onSurface.copy(alpha = 0.38f),
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = stringResource(R.string.ada_send),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/**
 * Ada's bubbles, the composer and the suggestions. Light theme paints the
 * background in surfaceVariant, so those use surface there to stand out.
 */
@Composable
private fun adaBubbleFill(): Color {
    val colors = MaterialTheme.colorScheme
    return if (colors.surfaceVariant == colors.background) colors.surface else colors.surfaceVariant
}

private val ComposerRadius = 24.dp
private val ComposerButtonSize = 36.dp

@Composable
private fun AdaAvatar(
    modifier: Modifier = Modifier,
    size: Dp = AvatarSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AdaPlushieFilled,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size * 0.62f),
        )
    }
}

@Composable
private fun AdaMessageRow(
    message: AdaMessage,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    onShowMore: (() -> Unit)? = null,
    /** The message before came from the same sender, so this one sits closer. */
    continuesRun: Boolean = false,
    /** Last in a run from one sender: it gets the avatar and the tail. */
    endsRun: Boolean = true,
    canAnswerRecipe: Boolean = true,
    onSaveRecipe: () -> Unit = {},
    onDropRecipe: () -> Unit = {},
    onOpenRecipe: () -> Unit = {},
    onEditRecipe: () -> Unit = {},
) {
    val fromUser = message.role == "user"
    val tail = if (endsRun) BubbleTail else BubbleRadius
    val shape = if (fromUser) {
        RoundedCornerShape(BubbleRadius, BubbleRadius, tail, BubbleRadius)
    } else {
        RoundedCornerShape(BubbleRadius, BubbleRadius, BubbleRadius, tail)
    }
    val container = if (fromUser) MaterialTheme.colorScheme.primary else adaBubbleFill()
    val contentColor = if (fromUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (continuesRun) 2.dp else 12.dp)
            .testTag(if (fromUser) "ada_user_message" else "ada_assistant_message"),
    ) {
        if (fromUser && message.photo != null) {
            AsyncImage(
                model = message.photo,
                contentDescription = stringResource(R.string.ada_photo_sent),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 12.dp, bottom = 4.dp)
                    .size(width = 160.dp, height = 200.dp)
                    .clip(RoundedCornerShape(BubbleRadius))
                    .testTag("ada_user_photo"),
            )
        }
        if (message.content.isNotEmpty() || message.streaming) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (fromUser) {
                    Spacer(modifier = Modifier.width(BubbleGutter))
                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    // Keeps its space when hidden so a run's bubbles line up.
                    AdaAvatar(
                        modifier = Modifier
                            .padding(end = AvatarGap)
                            .alpha(if (endsRun) 1f else 0f),
                    )
                }
                // Bubbles hug their text and only grow up to the gutter on the far side.
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .widthIn(max = BubbleMaxWidth),
                ) {
                    if (message.streaming && message.content.isEmpty()) {
                        AdaThinkingBubble(shape = shape, container = container, contentColor = contentColor)
                    } else {
                        Surface(
                            color = container,
                            contentColor = contentColor,
                            shape = shape,
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                if (message.content.isNotEmpty()) {
                                    SelectionContainer {
                                        Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                                if (message.streaming) {
                                    TypingDots(modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
                if (!fromUser) {
                    Spacer(modifier = Modifier.width(BubbleGutter / 2))
                }
            }
        }
        if (!fromUser && message.drinks.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(start = 12.dp + AvatarSize + AvatarGap, end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(message.drinks, key = { index, drink -> "${message.id}-${drink.id}-$index" }) { _, drink ->
                    DrinkCard(
                        item = drink,
                        onClick = { onDrinkClick(drink) },
                        onToggleSaved = { onToggleSaved(drink) },
                        modifier = Modifier.width(DrinkCardRailWidth),
                        posterHeight = DrinkCardRailHeight,
                    )
                }
            }
        }
        if (!fromUser && message.recipe != null) {
            AdaRecipeCard(
                recipe = message.recipe,
                canAnswer = canAnswerRecipe,
                onSave = onSaveRecipe,
                onDrop = onDropRecipe,
                onOpen = onOpenRecipe,
                onEdit = onEditRecipe,
                modifier = Modifier.padding(start = 12.dp + AvatarSize + AvatarGap, end = 12.dp, top = 8.dp),
            )
        }
        if (!fromUser && message.hasMore && onShowMore != null) {
            SuggestionChip(
                onClick = onShowMore,
                label = { Text(stringResource(R.string.ada_show_more)) },
                modifier = Modifier
                    .padding(start = 12.dp + AvatarSize + AvatarGap, top = 4.dp)
                    .testTag("ada_show_more"),
            )
        }
    }
}

private val AvatarSize = 32.dp
private val AvatarGap = 8.dp
private val BubbleGutter = 48.dp
private val BubbleRadius = 20.dp
private val BubbleTail = 6.dp
private val BubbleMaxWidth = 520.dp

private const val THINKING_PHRASE_MILLIS = 2_400L

@Composable
private fun AdaThinkingBubble(
    shape: Shape,
    container: Color,
    contentColor: Color,
) {
    val phrases = stringArrayResource(R.array.ada_thinking)
    var index by remember { mutableIntStateOf(0) }
    LaunchedEffect(phrases.size) {
        while (true) {
            delay(THINKING_PHRASE_MILLIS)
            index = (index + 1) % phrases.size
        }
    }
    Surface(
        modifier = Modifier.testTag("ada_thinking"),
        color = container,
        contentColor = contentColor,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .animateContentSize()
                .padding(start = 4.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = phrases[index],
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith
                        (fadeOut() + slideOutVertically { -it / 2 })
                },
                modifier = Modifier.padding(horizontal = 10.dp),
                label = "adaThinkingPhrase",
            ) { phrase ->
                Text(text = phrase, style = MaterialTheme.typography.bodyMedium)
            }
            TypingDots()
        }
    }
}

@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typingDots")
    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { dot ->
            val offset by transition.animateFloat(
                initialValue = 0f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1_200
                        0f at 0
                        -4f at 300
                        0f at 600
                    },
                    initialStartOffset = StartOffset(dot * 150),
                ),
                label = "typingDot$dot",
            )
            Box(
                modifier = Modifier
                    .offset(y = offset.dp)
                    .size(6.dp)
                    .background(LocalContentColor.current.copy(alpha = 0.6f), CircleShape),
            )
        }
    }
}

/** Start loading older messages when this many items from the oldest loaded one. */
private const val OLDER_PREFETCH = 3

@Composable
private fun AttachPhotoButton(
    enabled: Boolean,
    onChoosePhoto: () -> Unit,
    onTakePhoto: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { open = true },
            enabled = enabled,
            modifier = Modifier
                .size(ComposerButtonSize)
                .testTag("ada_attach_photo"),
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        ) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = stringResource(R.string.ada_photo_attach),
                modifier = Modifier.size(22.dp),
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ada_photo_take)) },
                leadingIcon = { Icon(Icons.Outlined.PhotoCamera, contentDescription = null) },
                onClick = {
                    open = false
                    onTakePhoto()
                },
                modifier = Modifier.testTag("ada_photo_take"),
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ada_photo_choose)) },
                leadingIcon = { Icon(Icons.Outlined.PhotoLibrary, contentDescription = null) },
                onClick = {
                    open = false
                    onChoosePhoto()
                },
                modifier = Modifier.testTag("ada_photo_choose"),
            )
        }
    }
}

/**
 * A fresh cache file the camera can write a photo for Ada into. Photos from
 * earlier days are deleted here; the ones from today may still show in the chat.
 */
private fun adaCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "ada_photos").apply { mkdirs() }
    val cutoff = System.currentTimeMillis() - CAMERA_PHOTO_KEEP_MS
    dir.listFiles()?.filter { it.lastModified() < cutoff }?.forEach { it.delete() }
    val file = File(dir, "photo-${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.photos", file)
}

private const val CAMERA_PHOTO_KEEP_MS = 24L * 60 * 60 * 1000
