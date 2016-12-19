package com.example.nik.mixology.utils;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import com.example.nik.mixology.Model.Cocktail;

import java.util.ArrayList;

import static com.example.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.example.nik.mixology.data.AlcoholicColumn._ID;

/**
 * Created by nik on 12/19/2016.
 */

public class ContentProviderHelperMethods {

    public static ArrayList<Cocktail> getDrinkListFromDatabase(Activity mAct) {

        ArrayList<Cocktail> mDrinkList = new ArrayList<>();
        Uri contentUri = CONTENT_URI;
        Cocktail cocktail = null;

        Cursor cursor = mAct.getContentResolver().query(contentUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                cocktail = new Cocktail();
                cocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
                cocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
                cocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

                mDrinkList.add(cocktail);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return mDrinkList;
    }

    public static boolean isDrinkInDatabase(Activity mAct, String id) {

        ArrayList<Cocktail> list = new ArrayList<>(getDrinkListFromDatabase(mAct));

        for (Cocktail listItem : list) {
            if (listItem.getmDrinkId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public static Cocktail getDrinkFromDatabase(Activity mActivity, String ID) {
        Cocktail cocktail = null;
        Uri contentUri = CONTENT_URI;

        Cursor cursor = mActivity.getContentResolver().query(contentUri, null, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            do {
                if (ID.equals(cursor.getString(cursor.getColumnIndex(_ID)))) {
                    cocktail = new Cocktail();
                    cocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
                    cocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
                    cocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));
                    break;
                }
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return cocktail;
    }


}
