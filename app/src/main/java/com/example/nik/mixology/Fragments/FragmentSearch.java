package com.example.nik.mixology.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nik.mixology.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSearch extends Fragment {

    public FragmentSearch() {
    }

    String text;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=  inflater.inflate(R.layout.fragment_activity_search, container, false);

        Bundle extras = getActivity().getIntent().getExtras();
        if(extras != null){
             text = extras.getString("Query");
        }

        TextView textView = (TextView) rootView.findViewById(R.id.SearchText);
        textView.setText(text);

        return rootView;
    }
}
