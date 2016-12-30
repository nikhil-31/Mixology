package com.example.nik.mixology.Activities;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements DrinkCursorAdapter.OnAdapterItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar = null;
    NavigationView navigationView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);


        FragmentAlcoholic fragment = new FragmentAlcoholic();
        android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("#Alcoholic");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
                intent.putExtra("Query", queryAdjusted);
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


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Cocktail cocktail) {
        FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment_details);
        if (detailsFragment == null) {
            Intent mCocktailDetailIntent = new Intent(this, ActivityDetails.class);
            mCocktailDetailIntent.putExtra("Cocktail", cocktail);
            startActivity(mCocktailDetailIntent);
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_Alcoholic) {

            FragmentAlcoholic fragment = new FragmentAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Alcoholic");

        } else if (id == R.id.nav_Non_Alcoholic) {

            FragmentNonAlcoholic fragment = new FragmentNonAlcoholic();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Non_Alcoholic");


        } else if (id == R.id.nav_gin) {

            FragmentGin fragment = new FragmentGin();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Gin");


        } else if (id == R.id.nav_vodka) {

            FragmentVodka fragment = new FragmentVodka();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Vodka");

        } else if (id == R.id.nav_cocktail_glass) {

            FragmentCocktailGlass fragment = new FragmentCocktailGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Cocktail_Glass");


        } else if (id == R.id.nav_Highball_Glass) {

            FragmentHighballGlass fragment = new FragmentHighballGlass();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Highball_glass");

        } else if (id == R.id.nav_Ordinary_Drink) {

            FragmentOrdinaryDrink fragment = new FragmentOrdinaryDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Ordinary_drink");


        } else if (id == R.id.nav_Cocktail) {
            FragmentCocktail fragment = new FragmentCocktail();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Cocktail");

        } else if (id == R.id.Saved_Cocktails) {

            FragmentSavedDrink fragment = new FragmentSavedDrink();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Saved_Cocktail");

        } else if (id == R.id.nav_randomixer) {

            FragmentRandomixer fragment = new FragmentRandomixer();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            toolbar.setTitle("#Randomixer");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
