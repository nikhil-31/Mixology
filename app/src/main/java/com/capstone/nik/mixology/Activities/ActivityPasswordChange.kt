package com.capstone.nik.mixology.Activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.capstone.nik.mixology.ui.auth.PasswordChangeScreen
import com.capstone.nik.mixology.ui.theme.MixologyTheme

class ActivityPasswordChange : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MixologyTheme {
                PasswordChangeScreen(
                    onBack = { finish() },
                    onReset = { },
                )
            }
        }
    }
}
