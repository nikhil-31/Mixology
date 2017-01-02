package com.example.nik.mixology.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.example.nik.mixology.Model.Cocktail;

import java.util.ArrayList;
import java.util.Vector;

import static com.example.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.example.nik.mixology.data.AlcoholicColumn._ID;
import static com.example.nik.mixology.data.DrinkProvider.SavedDrink.withId;

/**
 * Created by nik on 12/19/2016.
 */

public class ContentProviderHelperMethods {

    public static ArrayList<Cocktail> getDrinkListFromDatabase(Activity mAct,Uri uri) {

        ArrayList<Cocktail> mDrinkList = new ArrayList<>();

        Cocktail cocktail = null;

        Cursor cursor = mAct.getContentResolver().query(uri,
                null,
                null,
                null,
                null);

        assert cursor != null;
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

    public static boolean isDrinkInDatabase(Activity mAct, String id,Uri contentUri) {

        ArrayList<Cocktail> list = new ArrayList<>(getDrinkListFromDatabase(mAct,contentUri));

        for (Cocktail listItem : list) {
            if (listItem.getmDrinkId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public static void insertBulkData(Uri uri, ArrayList<Cocktail> mArrayList, Activity mAct) {

        Vector<ContentValues> cVVector = new Vector<ContentValues>(mArrayList.size());

        final String _id = "_id";
        final String Name = "name";
        final String Thumb = "thumb";

        for (Cocktail cocktail : mArrayList) {

            ContentValues contentValues = new ContentValues();
            String id = cocktail.getmDrinkId();
            boolean isThere = ContentProviderHelperMethods.isDrinkInDatabase(mAct, id, uri);
            if (!isThere) {

                contentValues.put(_id, cocktail.getmDrinkId());
                contentValues.put(Name, cocktail.getmDrinkName());
                contentValues.put(Thumb, cocktail.getmDrinkThumb());

                cVVector.add(contentValues);

            }
        }
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mAct.getContentResolver().bulkInsert(uri, cvArray);

        }

    }

    public static void insertData(Activity mAct,String id, ContentValues cv){

        mAct.getContentResolver().insert(withId(id), cv);

    }

    public static void deleteData(Activity mAct,String id){

        mAct.getContentResolver().delete(withId(id),
                null,
                null);

    }

    public static Cocktail getDrinkFromDatabase(Activity mActivity, String ID,Uri uri) {
        Cocktail cocktail = null;

        Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null);
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
