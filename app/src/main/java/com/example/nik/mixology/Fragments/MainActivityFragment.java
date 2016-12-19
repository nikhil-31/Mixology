package com.example.nik.mixology.Fragments;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.nik.mixology.Adapters.DrinkCursorAdapter;
import com.example.nik.mixology.Adapters.MainAdapter;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;
import com.example.nik.mixology.data.AlcoholicColumn;
import com.example.nik.mixology.utils.ContentProviderHelperMethods;
import com.example.nik.mixology.utils.Utils;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_ALCOHOLIC;
import static com.example.nik.mixology.data.DrinkProvider.Alcoholic.CONTENT_URI_ALCOHOLIC;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final int CURSOR_LOADER_ID = 0;

    public String STATE_COCKTAIL = "state_cocktails";

    private RecyclerView recyclerView;

    private ArrayList<Cocktail> mCocktailArrayList = new ArrayList<Cocktail>();

    private MainAdapter mAdapter;
    private DrinkCursorAdapter mDrinkAdapter;
    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;


    public MainActivityFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);


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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_main);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

//        mAdapter = new MainAdapter(getActivity(), getActivity());
        mDrinkAdapter = new DrinkCursorAdapter(getActivity(), null,getActivity() );
        recyclerView.setAdapter(mDrinkAdapter);

//        if (savedInstanceState != null) {
//            mCocktailArrayList = savedInstanceState.getParcelableArrayList(STATE_COCKTAIL);
//            mAdapter.setCocktailList(mCocktailArrayList);
//        } else {
//            sendJsonRequest();
//        }

        sendJsonRequest();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_COCKTAIL, mCocktailArrayList);
    }

    @Override
    public void onResume() {

        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);

    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                COCKTAIL_SEARCH_URL_ALCOHOLIC,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        try {

                            mCocktailArrayList.addAll(Utils.parseJSONResponse(response));
//                            mAdapter.setCocktailList(mCocktailArrayList);

//                            Cursor c = getActivity().getContentResolver().query(CONTENT_URI_ALCOHOLIC,
//                                    null,
//                                    null,
//                                    null,
//                                    null);
//                            Log.i(LOG_TAG, "cursor count: " + c.getCount());

                            Utils.insertData(CONTENT_URI_ALCOHOLIC, mCocktailArrayList, getActivity());

//                            getDataFromContentProvider();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }

//    public void insertData() {
//
//        Log.d(LOG_TAG, "Insert");
//
//        Vector<ContentValues> cVVector = new Vector<ContentValues>(mCocktailArrayList.size());
//
//        for (Cocktail cocktail : mCocktailArrayList) {
//
//            ContentValues contentValues = new ContentValues();
//            String id = cocktail.getmDrinkId();
//            boolean isThere = ContentProviderHelperMethods.isDrinkInDatabase(getActivity(), id,CONTENT_URI_ALCOHOLIC);
//            if (isThere) {
////                Toast.makeText(getActivity(), "Record Present", Toast.LENGTH_SHORT).show();
//            }
//            else {
//
//                contentValues.put(AlcoholicColumn._ID, cocktail.getmDrinkId());
//                contentValues.put(AlcoholicColumn.DRINK_NAME, cocktail.getmDrinkName());
//                contentValues.put(AlcoholicColumn.DRINK_THUMB, cocktail.getmDrinkThumb());
////                Toast.makeText(getActivity(), "Cocktail Added", Toast.LENGTH_SHORT).show();
//
//                cVVector.add(contentValues);
//            }
//
//        }
//        if (cVVector.size() > 0) {
//            ContentValues[] cvArray = new ContentValues[cVVector.size()];
//            cVVector.toArray(cvArray);
//            getContext().getContentResolver().bulkInsert(CONTENT_URI_ALCOHOLIC, cvArray);
//
////            getDataFromContentProvider();
//        }
//
//    }

    public void getDataFromContentProvider() {

        String[] projection = {
                AlcoholicColumn._ID,
                AlcoholicColumn.DRINK_NAME,
                AlcoholicColumn.DRINK_THUMB
        };

        Cursor cursor = getActivity().getContentResolver().query(
                CONTENT_URI_ALCOHOLIC,
                projection,
                null,
                null,
                null
        );

        String builder = null;

        int mIdIndex = cursor.getColumnIndex(AlcoholicColumn._ID);
        int mDrinkName = cursor.getColumnIndex(AlcoholicColumn.DRINK_NAME);
        int mDrinkThumb = cursor.getColumnIndex(AlcoholicColumn.DRINK_THUMB);

        assert cursor != null;
        while (cursor.moveToNext()) {

            String mId = cursor.getString(mIdIndex);
            String mName = cursor.getString(mDrinkName);
            String mThumb = cursor.getString(mDrinkThumb);

            builder = "Id" + mId + "\n name" + mName + "\n thumb" + mThumb;
            Toast.makeText(getActivity(), builder, Toast.LENGTH_SHORT).show();

        }

        cursor.close();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                CONTENT_URI_ALCOHOLIC,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        mDrinkAdapter.swapCursor(data);

//        String builder = null;
//
//        int mIdIndex = data.getColumnIndex(AlcoholicColumn._ID);
//        int mDrinkName = data.getColumnIndex(AlcoholicColumn.DRINK_NAME);
//        int mDrinkThumb = data.getColumnIndex(AlcoholicColumn.DRINK_THUMB);
//
//        assert data != null;
//        while (data.moveToNext()) {
//
//            String mId = data.getString(mIdIndex);
//            String mName = data.getString(mDrinkName);
//            String mThumb = data.getString(mDrinkThumb);
//
//            builder = "Id" + mId + "\n name" + mName + "\n thumb" + mThumb;
//            Toast.makeText(getActivity(), builder, Toast.LENGTH_SHORT).show();
//        }
//
//        data.close();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDrinkAdapter.swapCursor(null);
    }
}
