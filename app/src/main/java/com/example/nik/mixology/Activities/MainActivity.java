package com.example.nik.mixology.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.nik.mixology.Adapters.MainAdapter;
import com.example.nik.mixology.Fragments.ActivityDetailsFragment;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.R;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnAdapterItemSelectedListener {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Cocktail id) {
        ActivityDetailsFragment detailsFragment = (ActivityDetailsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_details);
        if(detailsFragment == null){
            Intent mCocktailDetailIntent = new Intent(this,ActivityDetails.class);
            mCocktailDetailIntent.putExtra("Cocktail",id);
            startActivity(mCocktailDetailIntent);
        }


    }
}
