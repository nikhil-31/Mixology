package com.capstone.nik.mixology.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.nik.mixology.R

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUp: (email: String, password: String) -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_up_navigation),
                tint = colors.onBackground,
            )
        }
        Image(
            painter = painterResource(R.drawable.login_screen_image),
            contentDescription = stringResource(R.string.content_desc_logo_img),
            modifier = Modifier.size(50.dp),
        )
        Text(
            text = stringResource(R.string.sign_up_create_new_account),
            modifier = Modifier.padding(top = 20.dp),
            fontSize = 18.sp,
            color = colors.onBackground,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            label = { Text(stringResource(R.string.sign_up_email_address)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        Button(
            onClick = { onSignUp(email.trim(), password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surfaceVariant,
                contentColor = colors.onSurfaceVariant,
            ),
        ) {
            Text(stringResource(R.string.sign_up_button_text))
        }
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(onClick = onPrivacyPolicy),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.privacy_policy_sign_up), fontSize = 12.sp, color = colors.onBackground)
            Text(
                text = stringResource(R.string.privacy_policy_link),
                modifier = Modifier.padding(start = 3.dp),
                fontSize = 12.sp,
                color = colors.secondary,
            )
        }
    }
}
