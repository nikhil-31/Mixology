package com.example.nik.mixology.Fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.nik.mixology.Model.CocktailDetails;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_ID;
import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_NAME;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSearch extends Fragment {

    public FragmentSearch() {
    }

    String Query;

    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;
    private TextView textView;
    private ArrayList<CocktailDetails> mCocktailDetails = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVolleySingleton = mVolleySingleton.getInstance();
        mRequestQueue = mVolleySingleton.getmRequestQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_search, container, false);

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            Query = extras.getString("Query");
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_search);

        textView.setText(Query);

        sendJsonRequest();
        return rootView;
    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                COCKTAIL_SEARCH_URL_BY_NAME + Query,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Log.d("Data", response.toString());

                            mCocktailDetails = parseJSONResponse(response);

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

    public ArrayList<CocktailDetails> parseJSONResponse(JSONObject response) throws JSONException {

        final String DRINKS = "drinks";
        final String NAME = "strDrink";
        final String CATEGORY = "strCategory";
        final String ALCOHOLIC = "strAlcoholic";
        final String GLASS = "strGlass";
        final String INSTRUCTIONS = "strInstructions";

        ArrayList<CocktailDetails> mdetailList = new ArrayList<>();

        if (response == null || response.length() == 0) {
            Toast.makeText(getActivity(), "Invalid input", Toast.LENGTH_LONG).show();
            return mdetailList;
        }

        JSONArray results = response.getJSONArray(DRINKS);

        for (int i = 0; i < results.length(); i++) {

            JSONObject jsonObject = results.getJSONObject(i);

            CocktailDetails details = new CocktailDetails();

            if (jsonObject.getString(NAME).length() != 0 && !jsonObject.isNull(NAME)) {
                details.setmName(jsonObject.getString(NAME));
            }

            if (jsonObject.getString(CATEGORY).length() != 0 && !jsonObject.isNull(CATEGORY)) {
                details.setmCategory(jsonObject.getString(CATEGORY));
            }

            if (jsonObject.getString(ALCOHOLIC).length() != 0 && !jsonObject.isNull(ALCOHOLIC)) {
                details.setmAlcoholic(jsonObject.getString(ALCOHOLIC));
            }
            if (jsonObject.getString(GLASS).length() != 0 && !jsonObject.isNull(GLASS)) {
                details.setmGlass(jsonObject.getString(GLASS));
            }

            if (jsonObject.getString(INSTRUCTIONS).length() != 0 && !jsonObject.isNull(INSTRUCTIONS)) {
                details.setmInstructions(jsonObject.getString(INSTRUCTIONS));
            }

            mdetailList.add(details);

        }
        return mdetailList;
    }


}
