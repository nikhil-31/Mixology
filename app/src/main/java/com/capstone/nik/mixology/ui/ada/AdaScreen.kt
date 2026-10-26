package com.capstone.nik.mixology.ui.ada

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.data.Drink
import com.capstone.nik.mixology.ui.components.DrinkCard
import com.capstone.nik.mixology.ui.components.DrinkCardRailHeight
import com.capstone.nik.mixology.ui.components.DrinkCardRailWidth
import com.capstone.nik.mixology.ui.mvi.CollectMviEffects

@Composable
fun AdaRoute(
    onDrinkClick: (Drink) -> Unit,
    viewModel: AdaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.onIntent(AdaIntent.Load)
    }
    CollectMviEffects(viewModel.effects) { effect ->
        when (effect) {
            is AdaEffect.OpenDrink -> onDrinkClick(effect.drink)
        }
    }
    AdaScreen(
        state = state,
        onEmailChanged = { viewModel.onIntent(AdaIntent.EmailChanged(it)) },
        onPasswordChanged = { viewModel.onIntent(AdaIntent.PasswordChanged(it)) },
        onDisplayNameChanged = { viewModel.onIntent(AdaIntent.DisplayNameChanged(it)) },
        onSignIn = { viewModel.onIntent(AdaIntent.SignIn) },
        onSignUp = { viewModel.onIntent(AdaIntent.SignUp) },
        onSignOut = { viewModel.onIntent(AdaIntent.SignOut) },
        onDraftChanged = { viewModel.onIntent(AdaIntent.DraftChanged(it)) },
        onSend = { viewModel.onIntent(AdaIntent.Send) },
        onSuggestion = { viewModel.onIntent(AdaIntent.UseSuggestion(it)) },
        onDrinkClick = { viewModel.onIntent(AdaIntent.OpenDrink(it)) },
        onToggleSaved = { viewModel.onIntent(AdaIntent.ToggleSaved(it)) },
    )
}

@Composable
fun AdaScreen(
    state: AdaUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onSignOut: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .testTag("ada_screen"),
    ) {
        if (!state.signedIn) {
            AdaSignIn(
                state = state,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onDisplayNameChanged = onDisplayNameChanged,
                onSignIn = onSignIn,
                onSignUp = onSignUp,
            )
        } else {
            AdaTranscript(
                state = state,
                onSignOut = onSignOut,
                onDraftChanged = onDraftChanged,
                onSend = onSend,
                onSuggestion = onSuggestion,
                onDrinkClick = onDrinkClick,
                onToggleSaved = onToggleSaved,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AdaSignIn(
    state: AdaUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    val canSubmit = state.email.isNotBlank() && state.password.isNotBlank() && !state.signingIn
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = AdaPlushieFilled,
                contentDescription = stringResource(R.string.nav_bottom_ada),
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = stringResource(R.string.ada_auth_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ada_email"),
            label = { Text(stringResource(R.string.ada_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ada_password"),
            label = { Text(stringResource(R.string.ada_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
        )
        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ada_name"),
            label = { Text(stringResource(R.string.ada_display_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )
        state.authError?.takeIf { it.isNotBlank() }?.let { detail ->
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("ada_auth_error"),
            )
        }
        Button(
            onClick = onSignIn,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ada_sign_in"),
        ) {
            Text(stringResource(R.string.ada_sign_in))
        }
        OutlinedButton(
            onClick = onSignUp,
            enabled = canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ada_sign_up"),
        ) {
            Text(stringResource(R.string.ada_sign_up))
        }
        if (state.signingIn) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(24.dp),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdaTranscript(
    state: AdaUiState,
    onSignOut: () -> Unit,
    onDraftChanged: (String) -> Unit,
    onSend: () -> Unit,
    onSuggestion: (String) -> Unit,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestions = listOf(
        stringResource(R.string.ada_suggestion_gin),
        stringResource(R.string.ada_suggestion_bitter),
        stringResource(R.string.ada_suggestion_easy),
    )
    val listState = rememberLazyListState()
    val tail = state.messages.lastOrNull()
    LaunchedEffect(state.messages.size, tail?.content, tail?.drinks?.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AdaPlushieFilled,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = state.email,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            TextButton(
                onClick = onSignOut,
                modifier = Modifier.testTag("ada_sign_out"),
            ) {
                Text(stringResource(R.string.ada_sign_out))
            }
        }
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
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = AdaPlushieFilled,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .size(72.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.ada_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    FlowRow(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        suggestions.forEachIndexed { index, label ->
                            SuggestionChip(
                                onClick = { onSuggestion(label) },
                                label = { Text(label) },
                                modifier = Modifier.testTag("ada_suggestion_$index"),
                            )
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("ada_messages"),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        AdaMessageRow(
                            message = message,
                            onDrinkClick = onDrinkClick,
                            onToggleSaved = onToggleSaved,
                        )
                    }
                }
            }
        }
        state.error?.let { detail ->
            Text(
                text = detail.ifBlank { stringResource(R.string.ada_error_generic) },
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("ada_error"),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChanged,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ada_draft"),
                placeholder = { Text(stringResource(R.string.ada_prompt)) },
                enabled = !state.sending,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
            )
            Button(
                onClick = onSend,
                enabled = state.draft.isNotBlank() && !state.sending && !state.loadingHistory,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .testTag("ada_send"),
            ) {
                Text(stringResource(R.string.ada_send))
            }
        }
    }
}

@Composable
private fun AdaMessageRow(
    message: AdaMessage,
    onDrinkClick: (Drink) -> Unit,
    onToggleSaved: (Drink) -> Unit,
) {
    val fromUser = message.role == "user"
    val shape = if (fromUser) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }
    val container = if (fromUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (fromUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(if (fromUser) "ada_user_message" else "ada_assistant_message"),
    ) {
        if (message.content.isNotEmpty() || message.streaming) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.86f),
                    color = container,
                    contentColor = contentColor,
                    shape = shape,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        if (message.content.isNotEmpty()) {
                            Text(text = message.content, style = MaterialTheme.typography.bodyLarge)
                        }
                        if (message.streaming) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(top = if (message.content.isEmpty()) 0.dp else 8.dp)
                                    .size(14.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            }
        }
        if (!fromUser && message.drinks.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
    }
}
