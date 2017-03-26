package com.capstone.nik.mixology.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.nik.mixology.Adapters.SearchAdapter;
import com.capstone.nik.mixology.Model.CocktailDetails;
import com.capstone.nik.mixology.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_NAME;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSearch extends Fragment {

    public FragmentSearch() {
    }

    private String Query;
    private TextView mEmptyView;
    private RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity_search, container, false);

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            Query = extras.getString(getString(R.string.search_intent_query));
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_search);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mSearchAdapter = new SearchAdapter(getActivity());
        mRecyclerView.setAdapter(mSearchAdapter);

        if (isOnline()) {
            OnTaskCompleted onTaskCompleted = new OnTaskCompleted() {
                @Override
                public void onMySearchTaskCompleted(ArrayList<CocktailDetails> cocktailDetailsArrayList) {
                    if (cocktailDetailsArrayList == null) {

                        mRecyclerView.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    }
                    else {

                        mSearchAdapter.setCocktailList(cocktailDetailsArrayList);
                    }
                }

            };

            MySearchTask mySearchTask = new MySearchTask(onTaskCompleted);
            mySearchTask.execute(COCKTAIL_SEARCH_URL_BY_NAME + Query);
        } else {
            Toast.makeText(getActivity(), R.string.no_network_available, Toast.LENGTH_LONG).show();
        }

        return rootView;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setAdapter(mSearchAdapter);
    }

    public class MySearchTask extends AsyncTask<String, Void, ArrayList<CocktailDetails>> {
        private final String LOG_TAG = MySearchTask.class.getSimpleName();

        private final OnTaskCompleted mListener;

        public MySearchTask(OnTaskCompleted listener) {
            super();
            mListener = listener;
        }

        @Override
        protected ArrayList<CocktailDetails> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String drinkJsonStr = null;

            try {

                String StringURL = params[0];
                URL url = new URL(StringURL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                drinkJsonStr = buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                JSONObject jsonObject = new JSONObject(drinkJsonStr);
                return parseJSONResponse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CocktailDetails> cocktailDetailsArrayList) {
            super.onPostExecute(cocktailDetailsArrayList);
            mListener.onMySearchTaskCompleted(cocktailDetailsArrayList);
        }
    }


    public ArrayList<CocktailDetails> parseJSONResponse(JSONObject response) throws JSONException {

        final String DRINKS = "drinks";
        final String ID = "idDrink";
        final String NAME = "strDrink";
        final String CATEGORY = "strCategory";
        final String ALCOHOLIC = "strAlcoholic";
        final String GLASS = "strGlass";
        final String INSTRUCTIONS = "strInstructions";
        final String THUMBNAIL = "strDrinkThumb";

        ArrayList<CocktailDetails> mdetailList = new ArrayList<>();

        if (response == null || response.length() == 0) {
            return mdetailList;
        }

        JSONObject object = new JSONObject(response.toString());
//        if (object.isNull(DRINKS)) {
//            Toast.makeText(getActivity(), getString(R.string.search_no_data_available), Toast.LENGTH_LONG).show();
//        }

        JSONArray results = response.getJSONArray(DRINKS);

        for (int i = 0; i < results.length(); i++) {

            JSONObject jsonObject = results.getJSONObject(i);

            CocktailDetails details = new CocktailDetails();

            if (jsonObject.getString(NAME).length() != 0 && !jsonObject.isNull(NAME)) {
                details.setmName(jsonObject.getString(NAME));
            }

            if (jsonObject.getString(CATEGORY).length() != 0 && !jsonObject.isNull(CATEGORY)) {
                details.setmCategory(jsonObject.getString(CATEGORY));
            }

            if (jsonObject.getString(ALCOHOLIC).length() != 0 && !jsonObject.isNull(ALCOHOLIC)) {
                details.setmAlcoholic(jsonObject.getString(ALCOHOLIC));
            }
            if (jsonObject.getString(GLASS).length() != 0 && !jsonObject.isNull(GLASS)) {
                details.setmGlass(jsonObject.getString(GLASS));
            }

            if (jsonObject.getString(INSTRUCTIONS).length() != 0 && !jsonObject.isNull(INSTRUCTIONS)) {
                details.setmInstructions(jsonObject.getString(INSTRUCTIONS));
            }

            if (jsonObject.getString(THUMBNAIL).length() != 0) {
                details.setmThumb(jsonObject.getString(THUMBNAIL));
            }

            if (jsonObject.getString(ID).length() != 0) {
                details.setmId(jsonObject.getString(ID));
            }

            mdetailList.add(details);

        }
        return mdetailList;
    }


}
