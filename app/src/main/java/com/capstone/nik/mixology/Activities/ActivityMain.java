package com.capstone.nik.mixology.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter;
import com.capstone.nik.mixology.Fragments.FragmentDetails;
import com.capstone.nik.mixology.Fragments.FragmentAlcoholic;
import com.capstone.nik.mixology.Fragments.FragmentHighballGlass;
import com.capstone.nik.mixology.Fragments.FragmentCocktail;
import com.capstone.nik.mixology.Fragments.FragmentCocktailGlass;
import com.capstone.nik.mixology.Fragments.FragmentGin;
import com.capstone.nik.mixology.Fragments.FragmentOrdinaryDrink;
import com.capstone.nik.mixology.Fragments.FragmentRandomixer;
import com.capstone.nik.mixology.Fragments.FragmentSavedDrink;
import com.capstone.nik.mixology.Fragments.FragmentVodka;
import com.capstone.nik.mixology.Fragments.FragmentNonAlcoholic;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.R;

//import com.crashlytics.android.Crashlytics;
//import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

// Repo moved from gitHub to bit bucket private repo
public class ActivityMain extends AppCompatActivity implements DrinkCursorAdapter.OnAdapterItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ActivityMain";

    public static final int RC_SIGN_IN = 1;
    private static final String ANONYMOUS = "anonymous";
    private static final String SELECTED_ID = "selected";

    private int mNavItemSelected;
    private NavigationView navigationView = null;

    // Firebase instance variables
//    private FirebaseAuth mFirebaseAuth;
//    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // Username
    private String mUsername;

    // Navigation header view
    private View mHeader;

    // Views for the navigation header
    private TextView mProfileNameText;
    private TextView mProfileEmailText;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "onAdLoaded: ");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "onAdFailedToLoad: " + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d(TAG, "onAdOpened: ");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d(TAG, "onAdLeftApplication: ");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.d(TAG, "onAdClosed: ");
            }
        });

        //Default UserName
        mUsername = ANONYMOUS;

        //Initialize Firebase components
//        mFirebaseAuth = FirebaseAuth.getInstance();

        // Initializing the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializes the Navigation Drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation Header that contains the name, email and profile image
        mHeader = navigationView.getHeaderView(0);

        mProfileImage = mHeader.findViewById(R.id.header_profile_image);
        mProfileNameText = mHeader.findViewById(R.id.header_profile_name);
        mProfileEmailText = mHeader.findViewById(R.id.header_profile_email);

        // Retaining the state
        mNavItemSelected = savedInstanceState == null ? R.id.nav_Alcoholic : savedInstanceState.getInt(SELECTED_ID);
        navigate(mNavItemSelected);

        // Checks if the user is signed in or not
//        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    // User is signed in
////          Crashlytics.setUserEmail(user.getEmail());
//                    if (user.getPhotoUrl() != null) {    // If the user is signed and there is a photo available
//                        onSignedInInitialize(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
//                    } else {
//                        onSignedInInitialize(user.getEmail());
//
//                    }
//                } else {
//                    // User is signed out
//                    onSignedOutTeardown();
//                }
//            }
//        };

        // Restoring the title after rotation
        if (savedInstanceState != null && getSupportActionBar() != null) {
            String title = savedInstanceState.getString("TITLE");
            getSupportActionBar().setTitle(title);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving the title
        outState.putInt(SELECTED_ID, mNavItemSelected);
        if (getSupportActionBar() != null) {
            CharSequence charSequence = getSupportActionBar().getTitle();
            if (charSequence != null) {
                String title = charSequence.toString();
                outState.putString("TITLE", title);
            }
        }
    }

    // When the user signs out the user name is set to anonymous.
    private void onSignedOutTeardown() {
        mUsername = ANONYMOUS;
    }

    private void onSignedInInitialize(String user, String email, Uri imageUrl) {

        if (mUsername != null && !user.isEmpty()) {
            mUsername = user;
            mProfileNameText.setText(mUsername);
        }
        if (email != null && !email.isEmpty()) {
            mProfileEmailText.setText(email);
        }
        if (imageUrl != null) {
            Picasso.get().load(imageUrl).error(R.drawable.empty_profile).into(mProfileImage);
        }
    }

    private void onSignedInInitialize(String email) {
        Picasso.get()
                .load(R.drawable.empty_profile)
                .into(mProfileImage);

        if (email != null && !email.isEmpty()) {
            mProfileEmailText.setText(email);
        }
    }

    private void setTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add the auth state listener when the activity is resumed.
//        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Auth state listener is removed when the activity is paused
//        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        // This is will close the drawer after something is selected.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();

        // The search query is sent to the search activity using the listener.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String queryAdjusted = query.replaceAll(" ", "%20");

                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                intent.putExtra(getString(R.string.intent_search_intent_query), queryAdjusted);
                startActivity(intent);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
//            mFirebaseAuth.signOut();
            mUsername = ANONYMOUS;
            Intent intent = new Intent(ActivityMain.this, ActivityLogin.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Cocktail cocktail) {
        // Check is the detail fragment is present in the main activity.
        FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (detailsFragment == null) {
            Intent intent = new Intent(this, ActivityDetails.class);
            intent.putExtra(getString(R.string.intent_details_intent_cocktail), cocktail);
            startActivity(intent);
        } else if (detailsFragment.isAdded()) {
            detailsFragment.updateContent(cocktail);
        }
    }

    // Navigate to the selected fragment when clicked in the navigation drawer.
    private void navigate(int id) {
        navigationView.setCheckedItem(id);
        if (id == R.id.nav_Alcoholic) {
            FragmentAlcoholic fragment = new FragmentAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_alcoholic));
        } else if (id == R.id.nav_Non_Alcoholic) {
            FragmentNonAlcoholic fragment = new FragmentNonAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_non_alcoholic));
        } else if (id == R.id.nav_gin) {
            FragmentGin fragment = new FragmentGin();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_gin));
        } else if (id == R.id.nav_vodka) {
            FragmentVodka fragment = new FragmentVodka();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_vodka));
        } else if (id == R.id.nav_cocktail_glass) {
            FragmentCocktailGlass fragment = new FragmentCocktailGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_cocktail_glass));
        } else if (id == R.id.nav_Highball_Glass) {
            FragmentHighballGlass fragment = new FragmentHighballGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_highball_glass));
        } else if (id == R.id.nav_Ordinary_Drink) {
            FragmentOrdinaryDrink fragment = new FragmentOrdinaryDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_ordinary_drink));
        } else if (id == R.id.nav_Cocktail) {
            FragmentCocktail fragment = new FragmentCocktail();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_cocktail));
        } else if (id == R.id.Saved_Cocktails) {
            FragmentSavedDrink fragment = new FragmentSavedDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_saved_cocktails));
        } else if (id == R.id.nav_randomixer) {
            FragmentRandomixer fragment = new FragmentRandomixer();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            setTitle(getString(R.string.nav_item_randomixer));
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mNavItemSelected = item.getItemId();
        navigate(mNavItemSelected);
        return true;
    }
}
