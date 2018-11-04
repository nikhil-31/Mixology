package com.capstone.nik.mixology.Fragments;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.birbit.android.jobqueue.JobManager;
import com.capstone.nik.mixology.Adapters.DrinkCursorAdapter;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.job.AlcoholFilterJob;

import javax.inject.Inject;

import static com.capstone.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentDrink#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentDrink extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
  private static final String TAG = "FragmentDrink";

  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  private String mParam1;
  private String mParam2;

  private static final int CURSOR_LOADER_ID = 0;
  private static final int CURSOR_LOADER_ID_ = 1;

  private Activity mActivity;

  @Inject
  JobManager mJobManager;

  public FragmentDrink() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment FragmentDrink.
   */
  public static FragmentDrink newInstance(String param1, String param2) {
    FragmentDrink fragment = new FragmentDrink();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

    RecyclerView recyclerView = rootView.findViewById(R.id.recycler_main);

    GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 2);
    recyclerView.setLayoutManager(gridLayoutManager);

    DrinkCursorAdapter mDrinkAdapter = new DrinkCursorAdapter(null, mActivity);
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
    return null;
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {

  }
}
