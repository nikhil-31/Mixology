package com.capstone.nik.mixology.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.nik.mixology.Adapters.SearchAdapter;
import com.capstone.nik.mixology.Fragments.FragmentDetails;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;
import com.capstone.nik.mixology.Network.remoteModel.Drink;
import com.capstone.nik.mixology.R;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivitySearch extends AppCompatActivity implements SearchAdapter.OnAdapterItemSelectedListener {
  private static final String TAG = "ActivitySearch";

  private String mQuery;

  private TextView mEmptyView;
  private RecyclerView mRecyclerView;

  private CocktailService service;
  private SearchAdapter mSearchAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    Toolbar toolbar = findViewById(R.id.toolbar);

    String query = getIntent().getStringExtra(getString(R.string.search_intent_query));
    String queryAdjusted = query.replaceAll("%20", " ");

    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(queryAdjusted);
    }

    mRecyclerView = findViewById(R.id.recycler_search);
    mEmptyView = findViewById(R.id.empty_view);

    LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mRecyclerView.setLayoutManager(mLinearLayoutManager);

    mSearchAdapter = new SearchAdapter(this);
    mRecyclerView.setAdapter(mSearchAdapter);

    final Retrofit.Builder builder = new Retrofit.Builder()
        .baseUrl(CocktailURLs.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create());

    Retrofit retrofit = builder.build();
    service = retrofit.create(CocktailService.class);

    sendQuery(queryAdjusted);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_search, menu);

    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
    searchView.setQueryHint(getResources().getString(R.string.action_search));
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        query = query.toLowerCase();

        sendQuery(query);
        return false;
      }

      @Override
      public boolean onQueryTextChange(String query) {


        return false;
      }
    });
    return true;
  }

  private void sendQuery(String mQuery) {


    Call<Cocktails> listCall = service.getSearchResults(mQuery);
    listCall.enqueue(new Callback<Cocktails>() {
      @Override
      public void onResponse(@NonNull Call<Cocktails> call, @NonNull Response<Cocktails> response) {
        Cocktails cocktails = response.body();
        if (cocktails != null) {
          List<Drink> drinks = cocktails.getDrinks();
          if (drinks != null && drinks.size() != 0) {
            mSearchAdapter.setCocktailList(drinks);
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
          } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
          }
        }

      }

      @Override
      public void onFailure(@NonNull Call<Cocktails> call, @NonNull Throwable t) {
        Log.e(TAG, "Error", t);
      }
    });
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
