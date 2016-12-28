package com.example.nik.mixology.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.nik.mixology.Adapters.SearchAdapter;
import com.example.nik.mixology.Fragments.FragmentDetails;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.R;

public class ActivitySearch extends AppCompatActivity implements SearchAdapter.OnAdapterItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String query = getIntent().getStringExtra("Query");
        String queryAdjusted = query.replaceAll("%20"," ");
        getSupportActionBar().setTitle(queryAdjusted);


    }

    @Override
    public void onItemSelected(Cocktail id) {
        FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment_details);
        if (detailsFragment == null) {
            Intent mCocktailDetailIntent = new Intent(this, ActivityDetails.class);
            mCocktailDetailIntent.putExtra("Cocktail", id);
            startActivity(mCocktailDetailIntent);
        }
    }
}
