package com.capstone.nik.mixology.Activities;

import android.content.Intent;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.capstone.nik.mixology.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class ActivityPasswordChange extends AppCompatActivity {
    private static final String TAG = "ActivityPasswordChange";

    ImageView mBackImageView;
    TextInputEditText mEmailEditText;

//    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

//        mFirebaseAuth = FirebaseAuth.getInstance();

        mBackImageView = findViewById(R.id.reset_password_back);
        mEmailEditText = findViewById(R.id.reset_password_username);

        mBackImageView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityPasswordChange.this, ActivityLogin.class);
            startActivity(intent);
        });
    }

    public void sendResetPasswordEmail(View view) {
        String resetEmail = mEmailEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(resetEmail)) {
//            mFirebaseAuth.sendPasswordResetEmail(resetEmail)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(ActivityPasswordChange.this, getString(R.string.reset_successful_message), Toast.LENGTH_SHORT).show();
//                            finish();
//                        } else if (!task.isSuccessful()) {
//
//                            try {
//                                throw task.getException();
//                            } catch (FirebaseAuthInvalidUserException e) {
//                                Toast.makeText(ActivityPasswordChange.this, getString(R.string.reset_unsuccessful), Toast.LENGTH_SHORT).show();
//                            } catch (Exception e) {
//                                Log.e(TAG, " Exception " + e);
//                            }
//                        }
//                    });
        } else {
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
    }
}
