package com.example.nik.mixology.Fragments;

import android.icu.util.Measure;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
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
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Model.CocktailDetails;
import com.example.nik.mixology.Model.Measures;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.ProcessingInstruction;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * A placeholder fragment containing a simple view.
 */
public class ActivityDetailsFragment extends Fragment {

    private Cocktail cocktail;

    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;

    private TextView mInstructions;
    private String mCocktailId;


    private ArrayList<CocktailDetails> cocktailDetailsArrayList = new ArrayList<>();

    public ActivityDetailsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_activity_details, container, false);

        cocktail = getActivity().getIntent().getParcelableExtra("Cocktail");

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        toolbar.setTitle(cocktail.getmDrinkName());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();

            }
        });
        toolbar.inflateMenu(R.menu.menu_activity_details);

        mCocktailId = cocktail.getmDrinkId();

        mInstructions = (TextView) v.findViewById(R.id.detail_instructions);
        mInstructions.setText(cocktail.getmDrinkName());

        sendJsonRequest();
        return v;
    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "http://www.thecocktaildb.com/api/json/v1/1/lookup.php?i="+mCocktailId ,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
//                        try {
////                            parseJSONResponse(response);
////                            parseJSONResponseMeasure(response);
//
//                            Log.d("response", response.toString());
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }



}
