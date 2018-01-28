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
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.withId;

/**
 * Created by nik on 12/19/2016.
 */

public class ContentProviderHelperMethods {

  public static final String ACTION_DATABASE_UPDATED = "com.example.nik.mixology.utils.ACTION_DATA_UPDATED";

  public static ArrayList<Cocktail> getDrinkListFromDatabase(Context context) {
    ArrayList<Cocktail> drinkList = new ArrayList<>();

    Cursor cursor = context.getContentResolver().query(CONTENT_URI_DRINK_SAVED
        , null
        , null
        , null
        , null);

    assert cursor != null;
    if (cursor.moveToFirst()) {
      do {
        Cocktail cocktail = new Cocktail();
        cocktail.setmDrinkId(cursor.getString(cursor.getColumnIndex(_ID)));
        cocktail.setmDrinkName(cursor.getString(cursor.getColumnIndex(DRINK_NAME)));
        cocktail.setmDrinkThumb(cursor.getString(cursor.getColumnIndex(DRINK_THUMB)));

        drinkList.add(cocktail);
      }
      while (cursor.moveToNext());
    }
    cursor.close();

    return drinkList;
  }

  public static boolean isDrinkSavedInDb(Context context, String id) {
    ArrayList<Cocktail> drinkList = new ArrayList<>(getDrinkListFromDatabase(context));

    for (Cocktail listItem : drinkList) {
      if (listItem.getmDrinkId().equals(id)) {
        return true;
      }
    }
    return false;
  }

  public static void insertData(Activity mAct, String id, ContentValues cv) {
    mAct.getContentResolver().insert(withId(id), cv);
    updateWidgets(mAct);
  }

  public static void deleteData(Activity mAct, String id) {
    mAct.getContentResolver().delete(withId(id), null, null);
    updateWidgets(mAct);
  }

  private static void updateWidgets(Context context) {
    // Setting the package ensures that only components in our app will receive the broadcast
    Intent dataUpdatedIntent = new Intent(ACTION_DATABASE_UPDATED).setPackage(context.getPackageName());
    context.sendBroadcast(dataUpdatedIntent);
  }

}
