package com.capstone.nik.mixology.job;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Network.CocktailService;
import com.capstone.nik.mixology.Network.CocktailURLs;
import com.capstone.nik.mixology.Network.remoteModel.Cocktails;
import com.capstone.nik.mixology.Network.remoteModel.Drink;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;

/**
 * Created by nik on 1/28/2018.
 */

public class AlcoholFilterJob extends Job {
    private static final String TAG = "AlcoholFilterJob";

    private Uri uri;
    private String filter;

    public AlcoholFilterJob(String uri, String filter) {
        super(new Params(1000).requireNetwork().groupBy("get_drink"));
        this.uri = Uri.parse(uri);
        this.filter = filter;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

        final Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(CocktailURLs.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        CocktailService service = retrofit.create(CocktailService.class);
        Call<Cocktails> listCall = service.getAlcoholFilter(filter);

        listCall.enqueue(new Callback<Cocktails>() {
            @Override
            public void onResponse(@NonNull Call<Cocktails> call, @NonNull Response<Cocktails> response) {
                Cocktails cocktails = response.body();

                if (cocktails != null) {
                    List<Drink> drinks = cocktails.getDrinks();
                    insertBulkData(uri, drinks, getApplicationContext());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cocktails> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), "Throwable " + t, Toast.LENGTH_SHORT).show();
                Log.v(TAG, "Error: ", t);
            }
        });
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return null;
    }

    private void insertBulkData(Uri uri, List<Drink> list, Context mAct) {
        Vector<ContentValues> cVVector = new Vector<>(list.size());

        final String _id = "_id";
        final String Name = "name";
        final String Thumb = "thumb";

        ArrayList<Cocktail> drinkList = new ArrayList<>();

        Cursor cursor = mAct.getContentResolver().query(uri
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

        for (Drink drink : list) {
            ContentValues contentValues = new ContentValues();

            String id = drink.getIdDrink();
            boolean isThere = isDrinkInDatabase(id, drinkList);

            if (drink.getStrDrinkThumb() != null && !drink.getStrDrinkThumb().equals("") && !drink.getStrDrinkThumb().equals("null")) {
                if (!isThere) {
                    contentValues.put(_id, drink.getIdDrink());
                    contentValues.put(Name, drink.getStrDrink());
                    contentValues.put(DRINK_THUMB, drink.getStrDrinkThumb());
                    Log.v(TAG, "url" + drink.getStrDrinkThumb());
                    cVVector.add(contentValues);
                }
            }
        }
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mAct.getContentResolver().bulkInsert(uri, cvArray);
        }
    }

    private boolean isDrinkInDatabase(String id, ArrayList<Cocktail> drinkList) {
        for (Cocktail listItem : drinkList) {
            if (listItem.getmDrinkId().equals(id)) {
                return true;
            }
        }
        return false;
    }


}
