package com.example.nik.mixology.Fragments;


import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;
import com.example.nik.mixology.utils.ContentProviderHelperMethods;
import com.example.nik.mixology.utils.Utils;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_NON_ALCOHOLIC;
import static com.example.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC;
import static com.example.nik.mixology.data.DrinkProvider.NonAlcoholic.CONTENT_URI_NON_ALCOHOLIC;


public class FragmentNonAlcoholic extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 1;

    private RecyclerView mRecyclerView;
    private DrinkCursorAdapter mDrinkAdapter;
    private TextView mEmptyTextView;

    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;

    public FragmentNonAlcoholic() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVolleySingleton = mVolleySingleton.getInstance();
        mRequestQueue = mVolleySingleton.getmRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_main);
        mEmptyTextView = (TextView) rootView.findViewById(R.id.empty_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mDrinkAdapter = new DrinkCursorAdapter(null, getActivity());
        mRecyclerView.setAdapter(mDrinkAdapter);

        Utils.sendNetworkJsonRequest(getActivity(), COCKTAIL_SEARCH_URL_NON_ALCOHOLIC, mRequestQueue, CONTENT_URI_NON_ALCOHOLIC);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                CONTENT_URI_NON_ALCOHOLIC,
                null,
                null,
                null,
                null);
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
