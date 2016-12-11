package com.example.nik.mixology.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.example.nik.mixology.Adapters.MainAdapter;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_ALCOHOLIC;
import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_NON_ALCOHOLIC;


public class NonAlcholicFragment extends Fragment {
    public String STATE_COCKTAIL = "state_cocktails";
    private String STATE_NULL = "null";

    private RecyclerView recyclerView;

    private ArrayList<Cocktail> mCocktailArrayList = new ArrayList<Cocktail>();

    private MainAdapter mAdapter;
    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;


    public NonAlcholicFragment() {
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

        mAdapter = new MainAdapter(getActivity(), getActivity());
        recyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null) {
            mCocktailArrayList = savedInstanceState.getParcelableArrayList(STATE_COCKTAIL);
            mAdapter.setCocktailList(mCocktailArrayList);
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

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                COCKTAIL_SEARCH_URL_NON_ALCOHOLIC,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {

                            mCocktailArrayList.addAll(parseJSONResponse(response));
                            mAdapter.setCocktailList(mCocktailArrayList);


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

    public ArrayList<Cocktail> parseJSONResponse(JSONObject response) throws JSONException {
        final String DRINKS = "drinks";
        final String COCKTAIL_NAME = "strDrink";
        final String COCKTAIL_THUMBNAIL = "strDrinkThumb";
        final String COCKTAIL_ID = "idDrink";

        ArrayList<Cocktail> data = new ArrayList<>();

        if (response == null || response.length() == 0) {
            return data;
        }


        JSONArray results = response.getJSONArray(DRINKS);

        for (int i = 0; i < results.length(); i++) {

            Cocktail cocktail = new Cocktail();

            JSONObject jsonObject = results.getJSONObject(i);

            String Thumb = jsonObject.getString(COCKTAIL_THUMBNAIL);

            if (Thumb != STATE_NULL) {

                cocktail.setmDrinkName(jsonObject.getString(COCKTAIL_NAME));
                cocktail.setmDrinkThumb(jsonObject.getString(COCKTAIL_THUMBNAIL));
                cocktail.setmDrinkId(jsonObject.getString(COCKTAIL_ID));

                data.add(cocktail);

            }


        }
        return data;

    }


}