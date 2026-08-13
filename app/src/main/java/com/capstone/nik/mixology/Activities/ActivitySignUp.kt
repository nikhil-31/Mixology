package com.capstone.nik.mixology.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.nik.mixology.ui.auth.PRIVACY_POLICY_URL
import com.capstone.nik.mixology.ui.auth.SignUpScreen
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivitySignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                SignUpScreen(
                    onBack = { finish() },
                    onSignUp = { _, _ -> },
                    onPrivacyPolicy = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                    },
                )
            }
        }
    }
}
