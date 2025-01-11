package com.capstone.nik.mixology.Fragments;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.loader.content.CursorLoader;

import com.birbit.android.jobqueue.JobManager;
import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.job.DrinkTypeFilterJob;


import javax.inject.Inject;

import static com.capstone.nik.mixology.data.DrinkProvider.Cocktail.CONTENT_URI_COCKTAIL;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCocktail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CURSOR_LOADER_ID = 0;

    private DrinkCursorAdapter mDrinkAdapter;
    private Activity mActivity;

    @Inject
    JobManager mJobManager;

    public FragmentCocktail() {
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
            ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
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

        mJobManager.addJobInBackground(new DrinkTypeFilterJob(CONTENT_URI_COCKTAIL.toString(), "Cocktail"));
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
                , CONTENT_URI_COCKTAIL
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