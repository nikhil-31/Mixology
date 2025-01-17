package com.capstone.nik.mixology.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.SupportVectorDrawablesButton;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
//import com.google.firebase.auth.FirebaseAuthInvalidUserException;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.GoogleAuthProvider;


public class ActivityLogin extends AppCompatActivity {
    private static final String TAG = "ActivityLogin";

    private static final int RC_SIGN_IN = 1;

    ProgressBar mLoginProgress;
    LinearLayout mLoginLinear;
    SupportVectorDrawablesButton mGoogleSignIn;
    SupportVectorDrawablesButton mEmailSignUp;
    TextInputEditText mEmailEditText;
    TextInputEditText mPasswordEditText;
    Button mLoginButton;
    TextView mForgotPassword;
    TextView mPrivacyPolicy;
    TextView mLoginTitleText;
    LinearLayout mPrivacyPolicyLink;

    // Google Login
//    private GoogleSignInClient mGoogleSignInClient;

    // Firebase auth state listener
//    private FirebaseAuth mFirebaseAuth;
//    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Progress dialog
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginProgress = findViewById(R.id.login_progress_bar);
        mLoginLinear = findViewById(R.id.login_linear_layout);
        mGoogleSignIn = findViewById(R.id.google_button);
        mEmailSignUp = findViewById(R.id.email_button);
        mEmailEditText = findViewById(R.id.login_username);
        mPasswordEditText = findViewById(R.id.login_password);
        mLoginButton = findViewById(R.id.login_button);
        mForgotPassword = findViewById(R.id.login_forgot_password);
        mPrivacyPolicy = findViewById(R.id.login_privacy_policy);
        mLoginTitleText = findViewById(R.id.login_title_text);
        mPrivacyPolicyLink = findViewById(R.id.login_privacy_policy_linear_layout);

        // Init firebase auth
//        mFirebaseAuth = FirebaseAuth.getInstance();

        // Init progress dialog
        mProgress = new ProgressDialog(this);

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
//
//         Init google sign in client
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Firebase Authentication Variables
//        mAuthStateListener = firebaseAuth -> {
//            FirebaseUser user = firebaseAuth.getCurrentUser();
//            if (user == null) {
//                setLogin();
//            } else {
//                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
//                startActivity(intent);
//            }
//        };

        // Login in button on click
        mLoginButton.setOnClickListener(v -> checkLogin());

        // Forgot password button on click
        mForgotPassword.setOnClickListener(v -> {
            Intent forgotPasswordIntent = new Intent(ActivityLogin.this, ActivityPasswordChange.class);
            startActivity(forgotPasswordIntent);
        });

        // Sign up with email button on click
        mEmailSignUp.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(ActivityLogin.this, ActivitySignUp.class);
            startActivity(signUpIntent);
        });

        // Sign in with google button
        mGoogleSignIn.setOnClickListener(v -> {
            googleSignIn();
            mProgress.setTitle(getString(R.string.login_progress_msg));
            mProgress.show();
        });

        mPrivacyPolicyLink.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://nikhil-31.github.io/Mixology/"));
            startActivity(browserIntent);
        });
    }

    /**
     * Start google sign IN process
     */
    private void googleSignIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Login with email and password
     */
    private void checkLogin() {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgress.setTitle(getString(R.string.login_message_login));
            mProgress.show();

//            mFirebaseAuth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            mProgress.dismiss();
//                        } else if (!task.isSuccessful()) {
//                            try {
//                                throw task.getException();
//                            } catch (FirebaseAuthInvalidCredentialsException e) {
//                                Toast.makeText(ActivityLogin.this, getString(R.string.login_username_password_invalid), Toast.LENGTH_SHORT).show();
//                            } catch (FirebaseAuthInvalidUserException e) {
//                                Toast.makeText(ActivityLogin.this, getString(R.string.login_user_not_present), Toast.LENGTH_SHORT).show();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            mProgress.dismiss();
//                        }
//                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.w(TAG, "Google sign in failed", e);
//                // [START_EXCLUDE]
//                Toast.makeText(this, getString(R.string.login_google_sign_in_failed), Toast.LENGTH_SHORT).show();
//                // [END_EXCLUDE]
//                mProgress.dismiss();
//            }
//        }
    }

    /**
     * Authentication google login with firebase login
     */
//    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

//        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
//            if (task.isSuccessful()) {
//                // Sign in success, update UI with the signed-in user's information
//                Log.d(TAG, "signInWithCredential:success");
//                FirebaseUser user = mFirebaseAuth.getCurrentUser();
//
//                Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
//                startActivity(intent);
//
//            } else {
//                Log.w(TAG, "signInWithCredential:failure", task.getException());
//            }
//            mProgress.dismiss();
//        });
//    }
//

    /**
     * Set login views as visible
     */
    private void setLogin() {
        mLoginLinear.setVisibility(View.VISIBLE);
        mEmailSignUp.setVisibility(View.VISIBLE);
        mLoginProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
}
