package com.capstone.nik.mixology.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.nik.mixology.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActivitySignUp extends AppCompatActivity {
  private static final String TAG = "ActivitySignUp";

  @BindView(R.id.register_email_address)
  EditText mEmailEditText;
  @BindView(R.id.register_password)
  EditText mPasswordEditText;
  @BindView(R.id.finish_sign_up_register)
  Button signUpTextView;
  @BindView(R.id.sign_up_back)
  ImageView signUpBack;
  @BindView(R.id.finish_sign_up_terms_and_conditions)
  TextView mPrivacyPolicy;
  @BindView(R.id.finish_sign_up_linear_layout)
  LinearLayout mPrivacyPolicyLink;

  private FirebaseAuth mFirebaseAuth;

  private ProgressDialog mProgress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sign_up);
    ButterKnife.bind(this);

    // Firebase Authentication Variables
    mFirebaseAuth = FirebaseAuth.getInstance();

    // Init progress dialog
    mProgress = new ProgressDialog(this);

    signUpTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        registerUser();
      }
    });

    signUpBack.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent gotoSignIn = new Intent(ActivitySignUp.this, ActivityLogin.class);
        startActivity(gotoSignIn);
      }
    });

    mPrivacyPolicyLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://nikhil-31.github.io/Mixology/"));
        startActivity(browserIntent);
      }
    });
  }

  /**
   * Register a new user
   */
  private void registerUser() {

    final String email = mEmailEditText.getText().toString().trim();
    final String password = mPasswordEditText.getText().toString().trim();

    if (!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password)) {

      mProgress.setTitle(getString(R.string.sign_up_title_text));
      mProgress.show();

      mFirebaseAuth.createUserWithEmailAndPassword(email, password)
          .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
              if (task.isSuccessful()) {
                mProgress.dismiss();

                Intent mainIntent = new Intent(ActivitySignUp.this, ActivityMain.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);

              } else if (!task.isSuccessful()) {
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                  Toast.makeText(ActivitySignUp.this, R.string.sign_up_user_exists, Toast.LENGTH_SHORT).show();
                }
                if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                  Toast.makeText(ActivitySignUp.this, R.string.sign_up_please_select_strong_password, Toast.LENGTH_SHORT).show();
                }
                mProgress.dismiss();
              }
            }
          });
    } else {
      Toast.makeText(this, R.string.sign_up_all_fields_required, Toast.LENGTH_SHORT).show();
    }
  }

}
