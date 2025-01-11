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

import com.birbit.android.jobqueue.JobManager;
import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.job.AlcoholFilterJob;


import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.loader.content.CursorLoader;

import javax.inject.Inject;

import static com.capstone.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentAlcoholic extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;

    private DrinkCursorAdapter mDrinkAdapter;
    private Activity mActivity;

    @Inject
    JobManager mJobManager;

    public FragmentAlcoholic() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LoaderManager.getInstance(this).initLoader(CURSOR_LOADER_ID, null, this);
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_main);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mDrinkAdapter = new DrinkCursorAdapter(null, mActivity);
        recyclerView.setAdapter(mDrinkAdapter);

        mJobManager.addJobInBackground(new AlcoholFilterJob(CONTENT_URI_ALCOHOLIC.toString(), "Alcoholic"));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(mActivity, CONTENT_URI_ALCOHOLIC, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<Cursor> loader, Cursor data) {
        mDrinkAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull androidx.loader.content.Loader<Cursor> loader) {
        mDrinkAdapter.swapCursor(null);
    }
}
