package com.example.nik.mixology.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.Fragments.FragmentDetails;
import com.example.nik.mixology.Fragments.FragmentAlcoholic;
import com.example.nik.mixology.Fragments.FragmentHighballGlass;
import com.example.nik.mixology.Fragments.FragmentCocktail;
import com.example.nik.mixology.Fragments.FragmentCocktailGlass;
import com.example.nik.mixology.Fragments.FragmentGin;
import com.example.nik.mixology.Fragments.FragmentOrdinaryDrink;
import com.example.nik.mixology.Fragments.FragmentRandomixer;
import com.example.nik.mixology.Fragments.FragmentSavedDrink;
import com.example.nik.mixology.Fragments.FragmentVodka;
import com.example.nik.mixology.Fragments.FragmentNonAlcoholic;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.email.SignInActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityMain extends AppCompatActivity implements DrinkCursorAdapter.OnAdapterItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    public static final int RC_SIGN_IN = 1;
    private static final String ANONYMOUS = "anonymous";
    private static final String SELECTED_ID = "selected";

    private int mNavItemSelected;
    private Toolbar toolbar = null;
    private NavigationView navigationView = null;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String mUsername;
    private View mHeader;

    private TextView mProfileNameText;
    private TextView mProfileEmailText;
    private CircleImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mUsername = ANONYMOUS;

        //Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mHeader = navigationView.getHeaderView(0);

        mProfileImage = (CircleImageView) mHeader.findViewById(R.id.header_profile_image);
        mProfileNameText = (TextView) mHeader.findViewById(R.id.header_profile_name);
        mProfileEmailText = (TextView) mHeader.findViewById(R.id.header_profile_email);

        mNavItemSelected = savedInstanceState == null ? R.id.nav_Alcoholic : savedInstanceState.getInt(SELECTED_ID);
        navigate(mNavItemSelected);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if (user.getPhotoUrl() != null) {
                        onSignedInInitialize(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
                    } else {
                        onSignedInInitialize(user.getEmail());

                    }


                } else {
                    // User is signed out
                    onSignedOutTeardown();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .setTheme(R.style.AppThemeFirebaseAuth)
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.ic_launcher1)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        if (savedInstanceState != null) {
            String title = savedInstanceState.getString("TITLE");
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ID, mNavItemSelected);
        String title = getSupportActionBar().getTitle().toString();
        outState.putString("TITLE", title);
    }

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
            Picasso.with(getApplicationContext())
                    .load(imageUrl)
                    .error(R.drawable.emptyprofile)
                    .into(mProfileImage);
        }
    }

    private void onSignedInInitialize(String email){

        Picasso.with(getApplicationContext())
                .load(R.drawable.emptyprofile)
                .into(mProfileImage);


        if (email != null && !email.isEmpty()) {
            mProfileEmailText.setText(email);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                String queryAdjusted = query.replaceAll(" ", "%20");

                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                intent.putExtra(getString(R.string.search_intent_query), queryAdjusted);
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
            FirebaseAuth.getInstance().signOut();

            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        startActivity(new Intent(ActivityMain.this, SignInActivity.class));
                        finish();
                    }
                }
            };
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Cocktail cocktail) {

        FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (detailsFragment == null) {
            Intent mCocktailDetailIntent = new Intent(this, ActivityDetails.class);
            mCocktailDetailIntent.putExtra(getString(R.string.details_intent_cocktail), cocktail);
            startActivity(mCocktailDetailIntent);
        } else {
            detailsFragment.updateContent(cocktail);
        }

    }


    private void navigate(int id) {
        navigationView.setCheckedItem(id);
        if (id == R.id.nav_Alcoholic) {

            FragmentAlcoholic fragment = new FragmentAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.alcoholic));

        } else if (id == R.id.nav_Non_Alcoholic) {

            FragmentNonAlcoholic fragment = new FragmentNonAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.non_alcoholic));


        } else if (id == R.id.nav_gin) {

            FragmentGin fragment = new FragmentGin();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.gin));


        } else if (id == R.id.nav_vodka) {

            FragmentVodka fragment = new FragmentVodka();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.vodka));

        } else if (id == R.id.nav_cocktail_glass) {

            FragmentCocktailGlass fragment = new FragmentCocktailGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.cocktail_glass));


        } else if (id == R.id.nav_Highball_Glass) {

            FragmentHighballGlass fragment = new FragmentHighballGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.highball_glass));

        } else if (id == R.id.nav_Ordinary_Drink) {

            FragmentOrdinaryDrink fragment = new FragmentOrdinaryDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.ordinary_drink));


        } else if (id == R.id.nav_Cocktail) {
            FragmentCocktail fragment = new FragmentCocktail();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.cocktail));

        } else if (id == R.id.Saved_Cocktails) {

            FragmentSavedDrink fragment = new FragmentSavedDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.saved_cocktails));

        } else if (id == R.id.nav_randomixer) {

            FragmentRandomixer fragment = new FragmentRandomixer();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle(getString(R.string.randomixer));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
