package com.capstone.nik.mixology.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// Repo moved from gitHub to bit bucket private repo
public class ActivityMain extends AppCompatActivity implements DrinkCursorAdapter.OnAdapterItemSelectedListener,
    NavigationView.OnNavigationItemSelectedListener {

  public static final int RC_SIGN_IN = 1;
  private static final String ANONYMOUS = "anonymous";
  private static final String SELECTED_ID = "selected";

  private int mNavItemSelected;
  private NavigationView navigationView = null;

  // Firebase instance variables
  private FirebaseAuth mFirebaseAuth;
  private FirebaseAuth.AuthStateListener mAuthStateListener;

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

    // Admob integration with my id
//    MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
    //TODO - Uncomment original Ad
    MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544/6300978111");

    AdView adView = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    adView.loadAd(adRequest);

    //Default UserName
    mUsername = ANONYMOUS;

    //Initialize Firebase components
    mFirebaseAuth = FirebaseAuth.getInstance();

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
    mAuthStateListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
          Crashlytics.setUserEmail(user.getEmail());
          if (user.getPhotoUrl() != null) {    // If the user is signed and there is a photo available
            onSignedInInitialize(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
          } else {
            onSignedInInitialize(user.getEmail());

          }
        } else {
          // User is signed out
          onSignedOutTeardown();

          // If the version is higher than lollipop then set the style in firebase or set no style
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(getSelectedProviders())
                    .setLogo(R.drawable.login_screen_image)
                    .setTheme(R.style.AppThemeFirebaseAuth)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN);
          } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(getSelectedProviders())
                    .setLogo(R.drawable.login_screen_image)
                    .setIsSmartLockEnabled(false)
                    .build(),
                RC_SIGN_IN);
          }
        }
      }
    };

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

  private List<AuthUI.IdpConfig> getSelectedProviders() {
    List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
    selectedProviders.add(new AuthUI.IdpConfig.GoogleBuilder().build());
    selectedProviders.add(new AuthUI.IdpConfig.EmailBuilder().build());
    return selectedProviders;
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
      Picasso.with(getApplicationContext()).load(imageUrl).error(R.drawable.empty_profile).into(mProfileImage);
    }
  }

  private void onSignedInInitialize(String email) {
    Picasso.with(getApplicationContext())
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
    mFirebaseAuth.addAuthStateListener(mAuthStateListener);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // Auth state listener is removed when the activity is paused
    mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
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
      mUsername = ANONYMOUS;
      FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
          FirebaseUser user = firebaseAuth.getCurrentUser();
          if (user == null) {
            finish();
          }
        }
      };
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onItemSelected(Cocktail cocktail) {
    // Check is the detail fragment is present in the main activity.
    FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment);
    if (detailsFragment == null) {
      Intent intent = new Intent(this, ActivityDetails.class);
      intent.putExtra(getString(R.string.details_intent_cocktail), cocktail);
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
      setTitle(getString(R.string.alcoholic));
    } else if (id == R.id.nav_Non_Alcoholic) {
      FragmentNonAlcoholic fragment = new FragmentNonAlcoholic();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.non_alcoholic));
    } else if (id == R.id.nav_gin) {
      FragmentGin fragment = new FragmentGin();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.gin));
    } else if (id == R.id.nav_vodka) {
      FragmentVodka fragment = new FragmentVodka();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.vodka));
    } else if (id == R.id.nav_cocktail_glass) {
      FragmentCocktailGlass fragment = new FragmentCocktailGlass();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.cocktail_glass));
    } else if (id == R.id.nav_Highball_Glass) {
      FragmentHighballGlass fragment = new FragmentHighballGlass();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.highball_glass));
    } else if (id == R.id.nav_Ordinary_Drink) {
      FragmentOrdinaryDrink fragment = new FragmentOrdinaryDrink();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.ordinary_drink));
    } else if (id == R.id.nav_Cocktail) {
      FragmentCocktail fragment = new FragmentCocktail();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.cocktail));
    } else if (id == R.id.Saved_Cocktails) {
      FragmentSavedDrink fragment = new FragmentSavedDrink();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.saved_cocktails));
    } else if (id == R.id.nav_randomixer) {
      FragmentRandomixer fragment = new FragmentRandomixer();
      android.support.v4.app.FragmentTransaction fragmentTransaction =
          getSupportFragmentManager().beginTransaction();
      fragmentTransaction.replace(R.id.fragment_container, fragment);
      fragmentTransaction.commit();
      setTitle(getString(R.string.randomixer));
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
