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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.Adapters.MainAdapter;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;
import com.example.nik.mixology.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_CHAMPAGNE_GLASS;
import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_ORDINARY;
import static com.example.nik.mixology.data.DrinkProvider.Cocktail.CONTENT_URI_COCKTAIL;
import static com.example.nik.mixology.data.DrinkProvider.OrdinaryDrink.CONTENT_URI_ORDINARY_DRINK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOrdinaryDrink extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public String STATE_COCKTAIL = "state_cocktails";
    private static final int CURSOR_LOADER_ID = 1;
    private RecyclerView recyclerView;
    private DrinkCursorAdapter mDrinkAdapter;
    private ArrayList<Cocktail> mCocktailArrayList = new ArrayList<Cocktail>();


    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;


    public FragmentOrdinaryDrink() {
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

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_main);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        mDrinkAdapter = new DrinkCursorAdapter(getActivity(), null, getActivity());
        recyclerView.setAdapter(mDrinkAdapter);

        if (savedInstanceState != null) {
            mCocktailArrayList = savedInstanceState.getParcelableArrayList(STATE_COCKTAIL);

        } else {
            sendJsonRequest();
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_COCKTAIL, mCocktailArrayList);
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                COCKTAIL_SEARCH_URL_ORDINARY,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {

                            mCocktailArrayList.addAll(Utils.parseJSONResponse(response));

                            Utils.insertData(CONTENT_URI_ORDINARY_DRINK, mCocktailArrayList, getActivity());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                CONTENT_URI_ORDINARY_DRINK,
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