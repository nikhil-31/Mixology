package com.example.nik.mixology.Fragments;

import android.content.ContentValues;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.nik.mixology.Adapters.IngredientsAdapter;
import com.example.nik.mixology.Model.Cocktail;
import com.example.nik.mixology.Model.CocktailDetails;
import com.example.nik.mixology.Model.Measures;
import com.example.nik.mixology.Network.VolleySingleton;
import com.example.nik.mixology.R;
import com.example.nik.mixology.utils.ContentProviderHelperMethods;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_ID;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.example.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.example.nik.mixology.data.AlcoholicColumn._ID;
import static com.example.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;
import static com.example.nik.mixology.data.DrinkProvider.SavedDrink.withId;


/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentDetails extends Fragment {

    private Cocktail cocktail;
    private CocktailDetails mCocktailDetails;

    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;

    private String mCocktailId;

    private TextView mInstructionsText;
    private TextView mAlcoholicText;
    private TextView mInstruction;
    private TextView mIngredients;
    private ImageView mDrinkImage;
    private TextView mDrinkName;
    private Toolbar mToolbar;
    private RecyclerView mIngredientsRecyclerView;
    private IngredientsAdapter mIngredientsAdapter;
    private Menu menu;
    private ArrayList<Measures> mMeasuresArrayList;
    private ImageView mDetailIcon;

    private boolean isInDatabase;

    public FragmentDetails() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVolleySingleton = mVolleySingleton.getInstance();
        mRequestQueue = mVolleySingleton.getmRequestQueue();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_activity_details, container, false);

        cocktail = getActivity().getIntent().getParcelableExtra("Cocktail");
        mCocktailId = cocktail.getmDrinkId();

        setHasOptionsMenu(true);


        mToolbar = (Toolbar) v.findViewById(R.id.toolbar);

        mToolbar.setTitle(cocktail.getmDrinkName());
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_back_black));
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();

            }
        });

        mToolbar.inflateMenu(R.menu.menu_activity_details);
//        menu = mToolbar.getMenu();

//        menu.findItem(R.id.action_add).setVisible(!isInDatabase);
//        menu.findItem(R.id.action_remove).setVisible(isInDatabase);

//        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//
//                int itemId = item.getItemId();
//                int addId = R.id.action_add;
//                int removeId = R.id.action_remove;
//
//                if (itemId == addId) {
////                    Toast.makeText(getActivity(), "Drink Added", Toast.LENGTH_SHORT).show();
//                    Snackbar.make(getActivity().findViewById(addId), "Drink Added", Snackbar.LENGTH_LONG).show();
//
//                    ContentValues cv = new ContentValues();
//                    cv.put(_ID, cocktail.getmDrinkId());
//                    cv.put(DRINK_NAME, cocktail.getmDrinkName());
//                    cv.put(DRINK_THUMB, cocktail.getmDrinkThumb());
//
//                    getActivity().getContentResolver().insert(withId(mCocktailId), cv);
//
//                    /* Re-querying the database to ensure that the data was added
//                     * Then, setting changing the menu item */
//                    boolean inDb = ContentProviderHelperMethods.isDrinkInDatabase(getActivity(), mCocktailId, CONTENT_URI_DRINK_SAVED);
//
//                    menu.findItem(R.id.action_add).setVisible(!inDb);
//                    menu.findItem(R.id.action_remove).setVisible(inDb);
//
//                    return true;
//                }
//
//                if (itemId == removeId) {
//
////                    Toast.makeText(getActivity(), "Drink Deleted", Toast.LENGTH_SHORT).show();
//                    Snackbar.make(getActivity().findViewById(removeId), "Drink Deleted", Snackbar.LENGTH_LONG).show();
//
//                    getActivity().getContentResolver().delete(withId(mCocktailId),
//                            null,
//                            null);
//
//                     /* Re-querying the database to ensure that the data was added
//                     * Then, setting changing the menu item */
//                    boolean inDb = ContentProviderHelperMethods.isDrinkInDatabase(getActivity(), mCocktailId, CONTENT_URI_DRINK_SAVED);
//
//                    menu.findItem(R.id.action_add).setVisible(!inDb);
//                    menu.findItem(R.id.action_remove).setVisible(inDb);
//
//                    return true;
//                }
//
//                return true;
//            }
//        });

        mInstructionsText = (TextView) v.findViewById(R.id.detail_instructions);
        mAlcoholicText = (TextView) v.findViewById(R.id.detail_alcoholic);
        mDrinkImage = (ImageView) v.findViewById(R.id.detail_imageView);
        mInstruction = (TextView) v.findViewById(R.id.detail_instructions_text);
        mIngredients = (TextView) v.findViewById(R.id.detail_ingredients_text);
        mDrinkName = (TextView) v.findViewById(R.id.detail_name);
        mDetailIcon = (ImageView) v.findViewById(R.id.detail_fav_button);

        sendJsonRequest();

        mIngredientsAdapter = new IngredientsAdapter(getActivity());
        mIngredientsRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_ingredients);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mIngredientsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mIngredientsRecyclerView.setAdapter(mIngredientsAdapter);

        mDetailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(getActivity(), mCocktailId, CONTENT_URI_DRINK_SAVED);

                if (isInDatabase) {
                    mDetailIcon.setImageResource(R.drawable.ic_fav_filled);

                    Snackbar.make(mDetailIcon, "Drink Deleted", Snackbar.LENGTH_LONG).show();

                    getActivity().getContentResolver().delete(withId(mCocktailId),
                            null,
                            null);

                    mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

                } else {
                    mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

                    Snackbar.make(mDetailIcon, "Drink Added", Snackbar.LENGTH_LONG).show();

                    ContentValues cv = new ContentValues();
                    cv.put(_ID, cocktail.getmDrinkId());
                    cv.put(DRINK_NAME, cocktail.getmDrinkName());
                    cv.put(DRINK_THUMB, cocktail.getmDrinkThumb());

                    getActivity().getContentResolver().insert(withId(mCocktailId), cv);

                    mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
                }

            }
        });



        return v;
    }

    private void setUIData() {

        mInstructionsText.setText(mCocktailDetails.getmInstructions());

        mAlcoholicText.setText(mCocktailDetails.getmAlcoholic());

        Picasso.with(getActivity())
                .load(cocktail.getmDrinkThumb())
                .error(R.drawable.empty_glass)
                .into(mDrinkImage);

        mInstruction.setText(getResources().getString(R.string.Instructions));
        mIngredients.setText(getResources().getString(R.string.Ingredients));
        mDrinkName.setText(cocktail.getmDrinkName());

        isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(getActivity(), mCocktailId, CONTENT_URI_DRINK_SAVED);

        if (isInDatabase) {
            mDetailIcon.setImageResource(R.drawable.ic_fav_filled);

        } else {
            mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

        }

    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                COCKTAIL_SEARCH_URL_BY_ID + mCocktailId,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Data", response.toString());
                        try {

                            mCocktailDetails = parseJSONResponse(response);
                            mMeasuresArrayList = parseJSONResponseMeasure(response);

                            setUIData();

                            mIngredientsAdapter.setMeasuresList(mMeasuresArrayList);

                            Log.d("Instructions", mCocktailDetails.getmInstructions());


                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        mRequestQueue.add(request);
    }

    public CocktailDetails parseJSONResponse(JSONObject response) throws JSONException {

        final String DRINKS = "drinks";
        final String NAME = "strDrink";
        final String CATEGORY = "strCategory";
        final String ALCOHOLIC = "strAlcoholic";
        final String GLASS = "strGlass";
        final String INSTRUCTIONS = "strInstructions";

        CocktailDetails details = new CocktailDetails();

        if (response == null || response.length() == 0) {
            return details;
        }
        JSONArray results = response.getJSONArray(DRINKS);

        for (int i = 0; i < results.length(); i++) {

            JSONObject jsonObject = results.getJSONObject(i);

            details.setmName(jsonObject.getString(NAME));
            details.setmCategory(jsonObject.getString(CATEGORY));
            details.setmAlcoholic(jsonObject.getString(ALCOHOLIC));
            details.setmGlass(jsonObject.getString(GLASS));
            details.setmInstructions(jsonObject.getString(INSTRUCTIONS));

        }
        return details;
    }

    public ArrayList<Measures> parseJSONResponseMeasure(JSONObject response) throws JSONException {

        final String DRINKS = "drinks";
        final String INGREDIENT_1 = "strIngredient1";
        final String INGREDIENT_2 = "strIngredient2";
        final String INGREDIENT_3 = "strIngredient3";
        final String INGREDIENT_4 = "strIngredient4";
        final String INGREDIENT_5 = "strIngredient5";
        final String INGREDIENT_6 = "strIngredient6";
        final String INGREDIENT_7 = "strIngredient7";
        final String INGREDIENT_8 = "strIngredient8";
        final String INGREDIENT_9 = "strIngredient9";
        final String INGREDIENT_10 = "strIngredient10";
        final String INGREDIENT_11 = "strIngredient11";
        final String INGREDIENT_12 = "strIngredient12";
        final String INGREDIENT_13 = "strIngredient13";
        final String INGREDIENT_14 = "strIngredient14";
        final String INGREDIENT_15 = "strIngredient15";
        final String MEASURE_1 = "strMeasure1";
        final String MEASURE_2 = "strMeasure2";
        final String MEASURE_3 = "strMeasure3";
        final String MEASURE_4 = "strMeasure4";
        final String MEASURE_5 = "strMeasure5";
        final String MEASURE_6 = "strMeasure6";
        final String MEASURE_7 = "strMeasure7";
        final String MEASURE_8 = "strMeasure8";
        final String MEASURE_9 = "strMeasure9";
        final String MEASURE_10 = "strMeasure10";
        final String MEASURE_11 = "strMeasure11";
        final String MEASURE_12 = "strMeasure12";
        final String MEASURE_13 = "strMeasure13";
        final String MEASURE_14 = "strMeasure14";
        final String MEASURE_15 = "strMeasure15";

        ArrayList<Measures> mMeasures = new ArrayList<>();

        JSONArray results = response.getJSONArray(DRINKS);

        for (int i = 0; i < results.length(); i++) {

            JSONObject jsonObject = results.getJSONObject(i);

            if (jsonObject.getString(INGREDIENT_1).length() != 0 && !jsonObject.isNull(INGREDIENT_1)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_1));
                measure.setMeasure(jsonObject.getString(MEASURE_1));
                mMeasures.add(measure);
            }

            if (jsonObject.getString(INGREDIENT_2).length() != 0  && !jsonObject.isNull(INGREDIENT_2) ) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_2));
                measure.setMeasure(jsonObject.getString(MEASURE_2));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_3).length() != 0  && !jsonObject.isNull(INGREDIENT_3)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_3));
                measure.setMeasure(jsonObject.getString(MEASURE_3));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_4).length() != 0  && !jsonObject.isNull(INGREDIENT_4)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_4));
                measure.setMeasure(jsonObject.getString(MEASURE_4));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_5).length() != 0  && !jsonObject.isNull(INGREDIENT_5)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_5));
                measure.setMeasure(jsonObject.getString(MEASURE_5));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_6).length() != 0  && !jsonObject.isNull(INGREDIENT_6)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_6));
                measure.setMeasure(jsonObject.getString(MEASURE_6));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_7).length() != 0  && !jsonObject.isNull(INGREDIENT_7)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_7));
                measure.setMeasure(jsonObject.getString(MEASURE_7));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_8).length() != 0  && !jsonObject.isNull(INGREDIENT_8)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_8));
                measure.setMeasure(jsonObject.getString(MEASURE_8));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_9).length() != 0  && !jsonObject.isNull(INGREDIENT_9)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_9));
                measure.setMeasure(jsonObject.getString(MEASURE_9));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_10).length() != 0  && !jsonObject.isNull(INGREDIENT_10)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_10));
                measure.setMeasure(jsonObject.getString(MEASURE_10));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_11).length() != 0  && !jsonObject.isNull(INGREDIENT_11)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_11));
                measure.setMeasure(jsonObject.getString(MEASURE_11));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_12).length() != 0  && !jsonObject.isNull(INGREDIENT_12)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_12));
                measure.setMeasure(jsonObject.getString(MEASURE_12));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_13).length() != 0  && !jsonObject.isNull(INGREDIENT_13)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_13));
                measure.setMeasure(jsonObject.getString(MEASURE_13));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_14).length() != 0  && !jsonObject.isNull(INGREDIENT_14)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_14));
                measure.setMeasure(jsonObject.getString(MEASURE_14));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_15).length() != 0  && !jsonObject.isNull(INGREDIENT_15)) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_15));
                measure.setMeasure(jsonObject.getString(MEASURE_15));
                mMeasures.add(measure);
            }
        }
        return mMeasures;
    }


}
