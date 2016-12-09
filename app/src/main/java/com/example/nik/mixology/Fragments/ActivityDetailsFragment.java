package com.example.nik.mixology.Fragments;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ActivityDetailsFragment extends Fragment {

    private Cocktail cocktail;
    private CocktailDetails mCocktailDetails;

    // Volley
    private RequestQueue mRequestQueue;
    private VolleySingleton mVolleySingleton;

    private String mCocktailId;

    private TextView mInstructionsText;
    private TextView mAlcoholicText;
    private TextView mInstructionText;
    private TextView mIngredientsText;
    private ImageView mDrinkImage;

    private RecyclerView mIngredientsRecyclerView;
    private IngredientsAdapter mIngredientsAdapter;

    private ArrayList<Measures> mMeasuresArrayList;

    public ActivityDetailsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_activity_details, container, false);

        cocktail = getActivity().getIntent().getParcelableExtra("Cocktail");

        mCocktailId = cocktail.getmDrinkId();


        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        toolbar.setTitle(cocktail.getmDrinkName());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(getActivity(), R.mipmap.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();

            }
        });
        toolbar.inflateMenu(R.menu.menu_activity_details);
        mInstructionsText = (TextView) v.findViewById(R.id.detail_instructions);
        mAlcoholicText = (TextView) v.findViewById(R.id.detail_alcoholic);
        mDrinkImage = (ImageView) v.findViewById(R.id.detail_imageView);
        mInstructionText = (TextView) v.findViewById(R.id.detail_instructions_text);
        mIngredientsText = (TextView) v.findViewById(R.id.detail_ingredients_text);

        sendJsonRequest();

        mIngredientsAdapter = new IngredientsAdapter(getActivity());
        mIngredientsRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_ingredients);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mIngredientsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mIngredientsRecyclerView.setAdapter(mIngredientsAdapter);


        return v;
    }

    private void setUIData() {

        mInstructionsText.setText(mCocktailDetails.getmInstructions());
        mAlcoholicText.setText(mCocktailDetails.getmAlcoholic());
        Picasso.with(getActivity())
                .load(cocktail.getmDrinkThumb())
                .error(R.drawable.empty_glass)
                .into(mDrinkImage);
        mInstructionText.setText(getResources().getString(R.string.Instructions));
        mIngredientsText.setText(getResources().getString(R.string.Ingredients));
    }

    private void sendJsonRequest() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                "http://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + mCocktailId,
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
        final String ALC0HOLIC = "strAlcoholic";
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

            details.setmAlcoholic(jsonObject.getString(ALC0HOLIC));

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
            if (jsonObject.getString(INGREDIENT_1).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_1));
                measure.setMeasure(jsonObject.getString(MEASURE_1));
                mMeasures.add(measure);
            }

            if (jsonObject.getString(INGREDIENT_2).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_2));
                measure.setMeasure(jsonObject.getString(MEASURE_2));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_3).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_3));
                measure.setMeasure(jsonObject.getString(MEASURE_3));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_4).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_4));
                measure.setMeasure(jsonObject.getString(MEASURE_4));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_5).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_5));
                measure.setMeasure(jsonObject.getString(MEASURE_5));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_6).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_6));
                measure.setMeasure(jsonObject.getString(MEASURE_6));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_7).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_7));
                measure.setMeasure(jsonObject.getString(MEASURE_7));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_8).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_8));
                measure.setMeasure(jsonObject.getString(MEASURE_8));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_9).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_9));
                measure.setMeasure(jsonObject.getString(MEASURE_9));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_10).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_10));
                measure.setMeasure(jsonObject.getString(MEASURE_10));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_11).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_11));
                measure.setMeasure(jsonObject.getString(MEASURE_11));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_12).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_12));
                measure.setMeasure(jsonObject.getString(MEASURE_12));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_13).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_13));
                measure.setMeasure(jsonObject.getString(MEASURE_13));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_14).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_14));
                measure.setMeasure(jsonObject.getString(MEASURE_14));
                mMeasures.add(measure);

            }

            if (jsonObject.getString(INGREDIENT_15).length() != 0) {
                Measures measure = new Measures();
                measure.setIngredient(jsonObject.getString(INGREDIENT_15));
                measure.setMeasure(jsonObject.getString(MEASURE_15));
                mMeasures.add(measure);
            }
        }
        return mMeasures;
    }


}
