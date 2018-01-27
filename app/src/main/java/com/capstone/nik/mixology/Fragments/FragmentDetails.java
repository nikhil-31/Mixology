package com.capstone.nik.mixology.Fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.capstone.nik.mixology.Model.Cocktail;
import com.capstone.nik.mixology.Model.CocktailDetails;
import com.capstone.nik.mixology.Model.Measures;
import com.capstone.nik.mixology.Network.MyApplication;
import com.capstone.nik.mixology.R;
import com.capstone.nik.mixology.utils.ContentProviderHelperMethods;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;

import static com.capstone.nik.mixology.Network.CocktailURLs.COCKTAIL_SEARCH_URL_BY_ID;
import static com.capstone.nik.mixology.Network.MyApplication.getAppContext;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_NAME;
import static com.capstone.nik.mixology.data.AlcoholicColumn.DRINK_THUMB;
import static com.capstone.nik.mixology.data.AlcoholicColumn._ID;
import static com.capstone.nik.mixology.data.DrinkProvider.SavedDrink.CONTENT_URI_DRINK_SAVED;


/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentDetails extends Fragment {

  private Cocktail mCocktail;

  // Volley
  @Inject
  RequestQueue mRequestQueue;

  private String mCocktailId;

  private TextView mInstructionsText;
  private TextView mAlcoholicText;
  private TextView mInstruction;
  private TextView mIngredients;
  private ImageView mDrinkImage;
  private TextView mDrinkName;
  private Toolbar mToolbar;
  private IngredientsAdapter mIngredientsAdapter;
  private ArrayList<Measures> mMeasuresArrayList;
  private ImageView mDetailIcon;
  private boolean isInDatabase;
  private Activity mActivity;

  public FragmentDetails() {
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivity = getActivity();
    if (mActivity != null) {
      ((MyApplication) mActivity.getApplication()).getApplicationComponent().inject(this);
    }
  }

  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                           Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_activity_details, container, false);

    mCocktail = mActivity.getIntent().getParcelableExtra(getString(R.string.details_intent_cocktail));

    setHasOptionsMenu(true);

//    MobileAds.initialize(getAppContext(), "ca-app-pub-3940256099942544~3347511713");
    //TODO - Uncomment original Ad
    MobileAds.initialize(getAppContext(), "ca-app-pub-3940256099942544/6300978111");

    AdView mAdView = v.findViewById(R.id.adViewDetails);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);

    mToolbar = v.findViewById(R.id.toolbar);

    mToolbar.setNavigationIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_back_black));
    mToolbar.setNavigationContentDescription(getString(R.string.content_desc_up_navigation));
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mActivity.finish();
      }
    });

    mToolbar.inflateMenu(R.menu.menu_activity_details);

    mInstructionsText = v.findViewById(R.id.detail_instructions);
    mAlcoholicText = v.findViewById(R.id.detail_alcoholic);
    mDrinkImage = v.findViewById(R.id.detail_imageView);
    mInstruction = v.findViewById(R.id.detail_instructions_text);
    mIngredients = v.findViewById(R.id.detail_ingredients_text);
    mDrinkName = v.findViewById(R.id.detail_name);
    mDetailIcon = v.findViewById(R.id.detail_fav_button);

    mIngredientsAdapter = new IngredientsAdapter(mActivity);
    RecyclerView ingredientsRecyclerView = v.findViewById(R.id.recycler_ingredients);
    ingredientsRecyclerView.setNestedScrollingEnabled(false);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);

    ingredientsRecyclerView.setLayoutManager(linearLayoutManager);
    ingredientsRecyclerView.setAdapter(mIngredientsAdapter);

    if (mCocktail != null) {
      startNetworkRequest(mCocktail);
    }

    return v;
  }

  public void updateContent(Cocktail cocktail) {
    startNetworkRequest(cocktail);

  }

  private void startNetworkRequest(final Cocktail cocktail) {
    mCocktailId = cocktail.getmDrinkId();
    sendJsonRequest(mCocktailId);
    mToolbar.setTitle(cocktail.getmDrinkName());
    mDrinkName.setText(cocktail.getmDrinkName());

    Picasso.with(mActivity)
        .load(cocktail.getmDrinkThumb())
        .error(R.drawable.empty_glass)
        .into(mDrinkImage);

    mInstruction.setText(getResources().getString(R.string.Instructions));
    mIngredients.setText(getResources().getString(R.string.Ingredients));

    isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(mActivity, mCocktailId, CONTENT_URI_DRINK_SAVED);

    if (isInDatabase) {
      mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
    } else {
      mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);
    }

    mDetailIcon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        isInDatabase = ContentProviderHelperMethods.isDrinkInDatabase(mActivity, mCocktailId, CONTENT_URI_DRINK_SAVED);

        if (isInDatabase) {
          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
          Snackbar.make(mDetailIcon, getString(R.string.drink_deleted), Snackbar.LENGTH_LONG).show();
          ContentProviderHelperMethods.deleteData(mActivity, mCocktailId);
          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);
        } else {
          mDetailIcon.setImageResource(R.drawable.ic_fav_unfilled_black);
          Snackbar.make(mDetailIcon, getString(R.string.drink_added), Snackbar.LENGTH_LONG).show();
          ContentValues cv = new ContentValues();
          cv.put(_ID, cocktail.getmDrinkId());
          cv.put(DRINK_NAME, cocktail.getmDrinkName());
          cv.put(DRINK_THUMB, cocktail.getmDrinkThumb());
          ContentProviderHelperMethods.insertData(mActivity, mCocktailId, cv);
          mDetailIcon.setImageResource(R.drawable.ic_fav_filled);
        }
      }
    });
  }

  private void shareRecipe(ArrayList<Measures> measuresArrayList, CocktailDetails cocktailDetails) {
    final StringBuilder builder = new StringBuilder();

    builder.append(getString(R.string.detail_share_sent_from_mixology)).append(" \n");
    builder.append(getString(R.string.detail_share_name)).append(" ").append(cocktailDetails.getmName()).append("\n");
    builder.append(getString(R.string.detail_share_alcoholic)).append(" ").append(cocktailDetails.getmAlcoholic()).append("\n");
    builder.append(getString(R.string.detail_share_instructions)).append(" ").append("\n").append(cocktailDetails.getmInstructions()).append("\n");
    builder.append(getString(R.string.detail_share_ingredients)).append("\n");

    for (int i = 0; i < measuresArrayList.size(); i++) {
      Measures measures = measuresArrayList.get(i);
      builder.append(measures.getIngredient()).append(" -- ").append(measures.getMeasure()).append("\n");
    }

    mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
          startActivity(Intent.createChooser(shareIntent(builder.toString()), getString(R.string.detail_share_via)));
        }
        return false;
      }
    });
  }

  public Intent shareIntent(String data) {
    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.detail_share_sent_from_mixology));
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, data);
    return sharingIntent;
  }

  private void sendJsonRequest(String id) {
    final CocktailDetails[] cocktailDetails = new CocktailDetails[1];

    JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
        COCKTAIL_SEARCH_URL_BY_ID + id,
        null,
        new Response.Listener<JSONObject>() {
          @Override
          public void onResponse(JSONObject response) {
            try {
              cocktailDetails[0] = parseJSONResponse(response);
              mInstructionsText.setText(cocktailDetails[0].getmInstructions());
              mAlcoholicText.setText(cocktailDetails[0].getmAlcoholic());
              mMeasuresArrayList = parseJSONResponseMeasure(response);
              if (isAdded() && mActivity != null) {
                shareRecipe(mMeasuresArrayList, cocktailDetails[0]);
              }
              mIngredientsAdapter.setMeasuresList(mMeasuresArrayList);
            } catch (JSONException e) {
              e.printStackTrace();
              Toast.makeText(mActivity, getString(R.string.Network_error), Toast.LENGTH_LONG).show();
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
