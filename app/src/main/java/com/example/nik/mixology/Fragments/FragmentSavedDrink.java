package com.example.nik.mixology.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSavedDrink extends Fragment {

    private RecyclerView recyclerView;
    private DrinkCursorAdapter mDrinkAdapter;


    public FragmentSavedDrink() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_main);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mDrinkAdapter = new DrinkCursorAdapter(getActivity(), null, getActivity());
        recyclerView.setAdapter(mDrinkAdapter);

        Toast.makeText(getActivity(),"Hi From Saved Drink Fragment",Toast.LENGTH_LONG).show();

        return rootView;
    }


}
