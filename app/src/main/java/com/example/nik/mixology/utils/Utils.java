package com.example.nik.mixology.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.example.nik.mixology.Model.Cocktail;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by nik on 12/19/2016.
 */

public class Utils {

    public static ArrayList<Cocktail> parseJSONResponse(JSONObject response) throws JSONException {
        final String DRINKS = "drinks";
        final String COCKTAIL_NAME = "strDrink";
        final String COCKTAIL_THUMBNAIL = "strDrinkThumb";
        final String COCKTAIL_ID = "idDrink";
        String STATE_NULL = "null";

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
