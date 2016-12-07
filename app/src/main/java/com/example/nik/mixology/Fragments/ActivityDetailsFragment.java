package com.example.nik.mixology.Fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nik.mixology.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ActivityDetailsFragment extends Fragment {

    public ActivityDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_details, container, false);
    }
}
