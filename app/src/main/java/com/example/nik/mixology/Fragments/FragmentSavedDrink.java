package com.example.nik.mixology.Fragments;


import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.Toast;

import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.R;

import static com.example.nik.mixology.data.DrinkProvider.Vodka.CONTENT_URI_VODKA;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSavedDrink extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    private DrinkCursorAdapter mDrinkAdapter;
    private static final int CURSOR_LOADER_ID = 1;

    public FragmentSavedDrink() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_main);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mDrinkAdapter = new DrinkCursorAdapter(getActivity(), null, getActivity());
        recyclerView.setAdapter(mDrinkAdapter);

        Toast.makeText(getActivity(), "Hi From Saved Drink Fragment", Toast.LENGTH_LONG).show();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                CONTENT_URI_VODKA,
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
