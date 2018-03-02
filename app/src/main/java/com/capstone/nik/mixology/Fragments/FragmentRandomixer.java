package com.capstone.nik.mixology.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.capstone.nik.mixology.Adapters.IngredientsAdapter;
import com.capstone.nik.mixology.Model.CocktailDetails;
import com.capstone.nik.mixology.Model.Measures;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_URL_RANDOM;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentRandomixer extends Fragment {

  public FragmentRandomixer() {
    // Required empty public constructor
  }

  private CocktailDetails mCocktailDetails;
  private ArrayList<Measures> mMeasuresArrayList;
  private Activity mActivity;

  // Volley
  @Inject
  RequestQueue mRequestQueue;

  private SwipeRefreshLayout mSwipeToRefreshLayout;
  private IngredientsAdapter mIngredientsAdapter;

  private TextView mInstructionsText;
  private TextView mAlcoholicText;
  private TextView mInstruction;
  private TextView mIngredients;
  private ImageView mDrinkImage;
  private TextView mDrinkName;
  private ImageView mDetailIcon;

  private boolean isInDatabase;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_randomixer, container, false);
    setHasOptionsMenu(true);
    mSwipeToRefreshLayout = rootView.findViewById(R.id.swipeToRefreshLayout);

    mInstructionsText = rootView.findViewById(R.id.detail_instructions);
    mAlcoholicText = rootView.findViewById(R.id.detail_alcoholic);
    mDrinkImage = rootView.findViewById(R.id.detail_imageView);
    mInstruction = rootView.findViewById(R.id.detail_instructions_text);
    mIngredients = rootView.findViewById(R.id.detail_ingredients_text);
    mDrinkName = rootView.findViewById(R.id.detail_name);
    mDetailIcon = rootView.findViewById(R.id.detail_fav_button);

    mIngredientsAdapter = new IngredientsAdapter(mActivity);
    RecyclerView mIngredientsRecyclerView = rootView.findViewById(R.id.recycler_ingredients);

    LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);

    mIngredientsRecyclerView.setLayoutManager(mLinearLayoutManager);
    mIngredientsRecyclerView.setAdapter(mIngredientsAdapter);
    mIngredientsRecyclerView.setNestedScrollingEnabled(false);

    mDetailIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mActivity, mCocktailDetails.getmId());

        if (isInDatabase) {
          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);

          Snackbar.make(mDetailIcon, getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();

          ContentProviderHelperMethods.deleteData(mActivity, mCocktailDetails.getmId());

          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

        } else {
          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);

          Snackbar.make(mDetailIcon, getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();

          ContentValues cv = new ContentValues();
          cv.put(_ID, mCocktailDetails.getmId());
          cv.put(DRINK_NAME, mCocktailDetails.getmName());
          cv.put(DRINK_THUMB, mCocktailDetails.getmThumb());

          ContentProviderHelperMethods.insertData(mActivity, mCocktailDetails.getmId(), cv);

          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
        }

      }
    });

    mSwipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        sendJsonRequest();
        mSwipeToRefreshLayout.setRefreshing(false);

      }
    });

    if (savedInstanceState == null) {
      sendJsonRequest();
    }
    return rootView;
  }

  public void setUIData() {
    mInstructionsText.setText(mCocktailDetails.getmInstructions());
    mAlcoholicText.setText(mCocktailDetails.getmAlcoholic());

    String url = mCocktailDetails.getmThumb();
    Picasso.with(mActivity).load(url).error(R.drawable.empty_glass)
        .into(mDrinkImage);

    mInstruction.setText(getResources().getString(R.string.Instructions));
    mIngredients.setText(getResources().getString(R.string.Ingredients));

    mDrinkName.setText(mCocktailDetails.getmName());
    isInDatabase = ContentProviderHelperMethods.isDrinkSavedInDb(mActivity, mCocktailDetails.getmId());

    if (isInDatabase) {
      mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
    } else {
      mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);
    }
  }

  private void sendJsonRequest() {
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
        COCKTAIL_URL_RANDOM,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              mCocktailDetails = parseJSONResponse(response);
              mMeasuresArrayList = parseJSONResponseMeasure(response);

              setUIData();
              mIngredientsAdapter.setMeasuresList(mMeasuresArrayList);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }, new Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
        Toast.makeText(mActivity, getString(R.string.no_network_available), Toast.LENGTH_LONG).show();
      }
    });
    mRequestQueue.add(request);
  }

  public CocktailDetails parseJSONResponse(JSONObject response) throws JSONException {
    final String DRINKS = "drinks";
    final String ID = "idDrink";
    final String NAME = "strDrink";
    final String CATEGORY = "strCategory";
    final String ALCOHOLIC = "strAlcoholic";
    final String GLASS = "strGlass";
    final String INSTRUCTIONS = "strInstructions";
    final String THUMB = "strDrinkThumb";

    CocktailDetails details = new CocktailDetails();

    if (response == null || response.length() == 0) {
      return details;
    }
    JSONArray results = response.getJSONArray(DRINKS);

    for (int i = 0; i < results.length(); i++) {
      JSONObject jsonObject = results.getJSONObject(i);
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
      if (jsonObject.getString(THUMB).length() != 0 && !jsonObject.isNull(THUMB)) {
        details.setmThumb(jsonObject.getString(THUMB));
      }
      if (jsonObject.getString(ID).length() != 0 && !jsonObject.isNull(ID)) {
        details.setmId(jsonObject.getString(ID));
      }
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

      if (jsonObject.getString(INGREDIENT_2).length() != 0 && !jsonObject.isNull(INGREDIENT_2)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_2));
        measure.setMeasure(jsonObject.getString(MEASURE_2));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_3).length() != 0 && !jsonObject.isNull(INGREDIENT_3)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_3));
        measure.setMeasure(jsonObject.getString(MEASURE_3));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_4).length() != 0 && !jsonObject.isNull(INGREDIENT_4)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_4));
        measure.setMeasure(jsonObject.getString(MEASURE_4));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_5).length() != 0 && !jsonObject.isNull(INGREDIENT_5)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_5));
        measure.setMeasure(jsonObject.getString(MEASURE_5));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_6).length() != 0 && !jsonObject.isNull(INGREDIENT_6)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_6));
        measure.setMeasure(jsonObject.getString(MEASURE_6));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_7).length() != 0 && !jsonObject.isNull(INGREDIENT_7)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_7));
        measure.setMeasure(jsonObject.getString(MEASURE_7));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_8).length() != 0 && !jsonObject.isNull(INGREDIENT_8)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_8));
        measure.setMeasure(jsonObject.getString(MEASURE_8));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_9).length() != 0 && !jsonObject.isNull(INGREDIENT_9)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_9));
        measure.setMeasure(jsonObject.getString(MEASURE_9));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_10).length() != 0 && !jsonObject.isNull(INGREDIENT_10)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_10));
        measure.setMeasure(jsonObject.getString(MEASURE_10));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_11).length() != 0 && !jsonObject.isNull(INGREDIENT_11)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_11));
        measure.setMeasure(jsonObject.getString(MEASURE_11));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_12).length() != 0 && !jsonObject.isNull(INGREDIENT_12)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_12));
        measure.setMeasure(jsonObject.getString(MEASURE_12));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_13).length() != 0 && !jsonObject.isNull(INGREDIENT_13)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_13));
        measure.setMeasure(jsonObject.getString(MEASURE_13));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_14).length() != 0 && !jsonObject.isNull(INGREDIENT_14)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_14));
        measure.setMeasure(jsonObject.getString(MEASURE_14));
        mMeasures.add(measure);
      }

      if (jsonObject.getString(INGREDIENT_15).length() != 0 && !jsonObject.isNull(INGREDIENT_15)) {
        Measures measure = new Measures();
        measure.setIngredient(jsonObject.getString(INGREDIENT_15));
        measure.setMeasure(jsonObject.getString(MEASURE_15));
        mMeasures.add(measure);
      }
    }
    return mMeasures;
  }


}
