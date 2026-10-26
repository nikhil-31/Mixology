package com.capstone.nik.mixology.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.play.findActivity

@Composable
fun AuthRoute(viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AuthScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onIntent: (AuthIntent) -> Unit,
) {
    val signUp = state.mode == AuthMode.SIGN_UP
    val focusManager = LocalFocusManager.current
    val activity = LocalContext.current.findActivity()
    val submit = {
        focusManager.clearFocus()
        onIntent(AuthIntent.Submit)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
            .testTag("auth_screen"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
            AnimatedContent(targetState = signUp, label = "authTitle") { creating ->
                Text(
                    text = stringResource(if (creating) R.string.auth_create_title else R.string.auth_welcome_back),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Text(
                text = stringResource(if (signUp) R.string.auth_sign_up_subtitle else R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            AnimatedVisibility(visible = signUp) {
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { onIntent(AuthIntent.DisplayNameChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name"),
                    label = { Text(stringResource(R.string.auth_name)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )
            }
            OutlinedTextField(
                value = state.email,
                onValueChange = { onIntent(AuthIntent.EmailChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email"),
                label = { Text(stringResource(R.string.auth_email)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = { onIntent(AuthIntent.PasswordChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password"),
                label = { Text(stringResource(R.string.auth_password)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { onIntent(AuthIntent.TogglePasswordVisible) }) {
                        Icon(
                            imageVector = if (state.passwordVisible) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                            contentDescription = stringResource(
                                if (state.passwordVisible) R.string.auth_hide_password else R.string.auth_show_password,
                            ),
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (state.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
            state.error?.takeIf { it.isNotBlank() }?.let { detail ->
                Text(
                    text = detail,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_error"),
                )
            }
            Button(
                onClick = submit,
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp)
                    .testTag("auth_submit"),
            ) {
                if (state.submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(stringResource(if (signUp) R.string.auth_sign_up else R.string.auth_sign_in))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.auth_or),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    activity?.let { onIntent(AuthIntent.SignInWithApple(it)) }
                },
                enabled = !state.submitting && activity != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_apple"),
            ) {
                Text(stringResource(R.string.auth_continue_with_apple))
            }
            OutlinedButton(
                onClick = {
                    focusManager.clearFocus()
                    activity?.let { onIntent(AuthIntent.SignInWithGoogle(it)) }
                },
                enabled = !state.submitting && activity != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_google"),
            ) {
                Text(stringResource(R.string.auth_continue_with_google))
            }
            TextButton(
                onClick = { onIntent(AuthIntent.SwitchMode) },
                enabled = !state.submitting,
                modifier = Modifier.testTag("auth_switch_mode"),
            ) {
                Text(
                    stringResource(
                        if (signUp) R.string.auth_switch_to_sign_in else R.string.auth_switch_to_sign_up,
                    ),
                )
            }
        }
    }
}
