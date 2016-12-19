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

    public static void insertData(Uri uri, ArrayList<Cocktail> mArrayList, Activity mAct) {


        Vector<ContentValues> cVVector = new Vector<ContentValues>(mArrayList.size());

        final String _id = "_id";
        final String Name = "name";
        final String Thumb = "thumb";


        for (Cocktail cocktail : mArrayList) {

            ContentValues contentValues = new ContentValues();
            String id = cocktail.getmDrinkId();
            boolean isThere = ContentProviderHelperMethods.isDrinkInDatabase(mAct, id, uri);
            if (isThere) {
//                Toast.makeText(mAct, "Record Present " + id, Toast.LENGTH_SHORT).show();
            } else {
                contentValues.put(_id, cocktail.getmDrinkId());
                contentValues.put(Name, cocktail.getmDrinkName());
                contentValues.put(Thumb, cocktail.getmDrinkThumb());
//                Toast.makeText(mAct, "Cocktail Added", Toast.LENGTH_SHORT).show();

                cVVector.add(contentValues);

            }

        }
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mAct.getContentResolver().bulkInsert(uri, cvArray);


        }

    }

}