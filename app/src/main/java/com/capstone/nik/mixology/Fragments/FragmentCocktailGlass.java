package com.capstone.nik.mixology.Fragments;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.Utils;

import javax.inject.Inject;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_COCKTAIL_GLASS;
import static com.capstone.nik.mixology.data.DrinkProvider.CocktailGlass.CONTENT_URI_COCKTAIL_GLASS;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCocktailGlass extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final int CURSOR_LOADER_ID = 0;

  private DrinkCursorAdapter mDrinkAdapter;
  private Activity mActivity;

  // Volley
  @Inject
  RequestQueue mRequestQueue;

  public FragmentCocktailGlass() {
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    RecyclerView recyclerView = rootView.findViewById(R.id.recycler_main);

    GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2);
    recyclerView.setLayoutManager(gridLayoutManager);

    mDrinkAdapter = new DrinkCursorAdapter(null, mActivity);
    recyclerView.setAdapter(mDrinkAdapter);

    Utils.sendNetworkJsonRequest(mActivity, COCKTAIL_SEARCH_URL_COCKTAIL_GLASS, mRequestQueue, CONTENT_URI_COCKTAIL_GLASS);
    return rootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(mActivity
        , CONTENT_URI_COCKTAIL_GLASS
        , null
        , null
        , null
        , null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mDrinkAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mDrinkAdapter.swapCursor(null);
  }
}

