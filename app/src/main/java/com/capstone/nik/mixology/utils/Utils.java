package com.capstone.nik.mixology.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by nik on 12/19/2016.
 */

public class Utils {

    public static ArrayList<Cocktail> parseJSONResponse(JSONObject response) throws JSONException {

        final String DRINKS = "drinks";
        final String COCKTAIL_NAME = "strDrink";
        final String COCKTAIL_THUMBNAIL = "strDrinkThumb";
        final String COCKTAIL_ID = "idDrink";
        final String STATE_NULL = "null";

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

                if (jsonObject.getString(COCKTAIL_NAME).length() != 0 && !jsonObject.isNull(COCKTAIL_NAME)) {
                    cocktail.setmDrinkName(jsonObject.getString(COCKTAIL_NAME));
                }
                if (jsonObject.getString(COCKTAIL_THUMBNAIL).length() != 0 && !jsonObject.isNull(COCKTAIL_THUMBNAIL)) {
                    cocktail.setmDrinkThumb(jsonObject.getString(COCKTAIL_THUMBNAIL));
                }
                if (jsonObject.getString(COCKTAIL_ID).length() != 0 && !jsonObject.isNull(COCKTAIL_ID)) {
                    cocktail.setmDrinkId(jsonObject.getString(COCKTAIL_ID));
                }
                data.add(cocktail);
            }
        }
        return data;

    }

    public static void sendNetworkJsonRequest(final Activity activity, String url, RequestQueue requestQueue, final Uri uri) {

        final ArrayList<Cocktail> mCocktailArrayList = new ArrayList<>();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            mCocktailArrayList.addAll(Utils.parseJSONResponse(response));
                            ContentProviderHelperMethods.insertBulkData(uri, mCocktailArrayList, activity);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, activity.getString(R.string.no_network_available), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(request);

    }


}
