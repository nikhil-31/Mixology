package com.example.nik.mixology.Fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;

import org.json.JSONException;
import org.json.JSONObject;

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
    TextView textView;

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

        textView = (TextView) rootView.findViewById(R.id.SearchText);
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
                        Log.d("Data", response.toString());

                        Toast.makeText(getActivity(), "Response " + response, Toast.LENGTH_LONG).show();

                        textView.setText(response.toString());

//                            mCocktailDetails = parseJSONResponse(response);
//                            mMeasuresArrayList = parseJSONResponseMeasure(response);
//
//                            setUIData();
//
//                            mIngredientsAdapter.setMeasuresList(mMeasuresArrayList);
//
//                            Log.d("Instructions", mCocktailDetails.getmInstructions());


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }
}
