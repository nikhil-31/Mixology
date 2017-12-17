package com.capstone.nik.mixology.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.capstone.nik.mixology.Model.Cocktail;

import java.util.ArrayList;
import java.util.Vector;

import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.withId;

/**
 * Created by nik on 12/19/2016.
 */

public class ContentProviderHelperMethods {

  public static final String ACTION_DATABASE_UPDATED = "com.example.nik.mixology.utils.ACTION_DATA_UPDATED";

  public static ArrayList<Cocktail> getDrinkListFromDatabase(Activity mAct, Uri uri) {

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
      }
      while (cursor.moveToNext());
    }
    cursor.close();
    return mDrinkList;
  }

  public static boolean isDrinkInDatabase(Activity mAct, String id, Uri contentUri) {

    ArrayList<Cocktail> list = new ArrayList<>(getDrinkListFromDatabase(mAct, contentUri));

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

  public static void insertData(Activity mAct, String id, ContentValues cv) {
    mAct.getContentResolver().insert(withId(id), cv);
    updateWidgets(mAct);
  }


  public static void deleteData(Activity mAct, String id) {

    mAct.getContentResolver().delete(withId(id),
        null,
        null);
    updateWidgets(mAct);

  }

  private static void updateWidgets(Context context) {

    // Setting the package ensures that only components in our app will receive the broadcast
    Intent dataUpdatedIntent = new Intent(ACTION_DATABASE_UPDATED).setPackage(context.getPackageName());
    context.sendBroadcast(dataUpdatedIntent);
  }

}
