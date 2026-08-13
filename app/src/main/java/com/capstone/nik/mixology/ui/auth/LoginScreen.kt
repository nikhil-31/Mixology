package com.capstone.nik.mixology.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capstone.nik.mixology.R
import com.capstone.nik.mixology.ui.theme.MixologyGray
import com.capstone.nik.mixology.ui.theme.MixologyNavy
import com.capstone.nik.mixology.ui.theme.MixologyText

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onPrivacyPolicy: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.login_screen_image),
            contentDescription = stringResource(R.string.content_desc_logo_img),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 16.dp),
        )
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MixologyText,
        )
        Text(
            text = stringResource(R.string.login_sign_in_button_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            fontSize = 16.sp,
            color = MixologyText,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_username)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )
        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier
                .align(Alignment.End)
                .testTag("login_forgot_password"),
        ) {
            Text(stringResource(R.string.login_forgot_password), color = MixologyNavy)
        }
        Button(
            onClick = { onLogin(email.trim(), password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .testTag("login_submit"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MixologyGray,
                contentColor = MixologyText,
            ),
        ) {
            Text(stringResource(R.string.login_button_text))
        }
        Text(
            text = stringResource(R.string.login_or_sign_in_with),
            color = MixologyText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 4.dp)
                .testTag("login_google"),
        ) {
            Text(stringResource(R.string.fui_sign_in_with_google), color = MixologyText)
        }
        OutlinedButton(
            onClick = onSignUp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 4.dp)
                .testTag("login_sign_up"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        ) {
            Text(stringResource(R.string.fui_sign_in_with_email), color = MixologyNavy)
        }
        Row(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 24.dp)
                .testTag("login_privacy")
                .clickable(onClick = onPrivacyPolicy),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(stringResource(R.string.privacy_policy_sign_in), fontSize = 12.sp, color = MixologyText)
            Text(
                text = stringResource(R.string.privacy_policy_link),
                modifier = Modifier.padding(start = 3.dp),
                fontSize = 12.sp,
                color = MixologyNavy,
            )
        }
    }
}
