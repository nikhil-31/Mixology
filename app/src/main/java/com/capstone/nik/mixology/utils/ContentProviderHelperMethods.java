package com.capstone.nik.mixology.utils;

import android.content.Context;

import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.repository.DrinkRepository;

public class ContentProviderHelperMethods {

    public static final String ACTION_DATABASE_UPDATED = DrinkRepository.ACTION_DATABASE_UPDATED;

    private static DrinkRepository repository(Context context) {
        return ((MyApplication) context.getApplicationContext()).getApplicationComponent().drinkRepository();
    }

    public static boolean isDrinkSavedInDb(Context context, String id) {
        return repository(context).isSavedBlocking(id);
    }

    public static void insertData(Context context, Cocktail cocktail) {
        repository(context).saveBlocking(cocktail);
    }

    public static void deleteData(Context context, String id) {
        repository(context).unsaveBlocking(id);
    }
}
