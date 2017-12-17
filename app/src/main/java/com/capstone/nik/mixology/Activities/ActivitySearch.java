package com.capstone.nik.mixology.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.capstone.nik.mixology.Adapters.SearchAdapter;
import com.capstone.nik.mixology.Fragments.FragmentDetails;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.R;

public class ActivitySearch extends AppCompatActivity implements SearchAdapter.OnAdapterItemSelectedListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    String query = getIntent().getStringExtra(getString(R.string.search_intent_query));
    String queryAdjusted = query.replaceAll("%20", " ");
    getSupportActionBar().setTitle(queryAdjusted);


  }

  @Override
  public void onItemSelected(Cocktail id) {
    FragmentDetails detailsFragment = (FragmentDetails) getSupportFragmentManager().findFragmentById(R.id.fragment_details);

    if (detailsFragment == null) {

      Intent mCocktailDetailIntent = new Intent(this, ActivityDetails.class);
      mCocktailDetailIntent.putExtra(getString(R.string.details_intent_cocktail), id);
      startActivity(mCocktailDetailIntent);

    }
  }
}
